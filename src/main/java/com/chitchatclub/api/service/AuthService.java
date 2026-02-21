package com.chitchatclub.api.service;

import com.chitchatclub.api.dto.request.ChangePasswordRequest;
import com.chitchatclub.api.dto.request.LoginRequest;
import com.chitchatclub.api.dto.request.RegisterRequest;
import com.chitchatclub.api.dto.request.ResetPasswordRequest;
import com.chitchatclub.api.dto.response.AuthResponse;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.entity.PasswordResetToken;
import com.chitchatclub.api.entity.RefreshToken;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.VerificationToken;
import com.chitchatclub.api.exception.BadRequestException;
import com.chitchatclub.api.exception.ConflictException;
import com.chitchatclub.api.exception.ForbiddenException;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.PasswordResetTokenRepository;
import com.chitchatclub.api.repository.RefreshTokenRepository;
import com.chitchatclub.api.repository.UserRepository;
import com.chitchatclub.api.repository.VerificationTokenRepository;
import com.chitchatclub.api.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       EmailService emailService,
                       VerificationTokenRepository verificationTokenRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnglishLevelType(request.englishLevelType());
        user.setEnglishLevelValue(request.englishLevelValue());
        user.setEmailVerified(false);

        user = userRepository.save(user);
        emailService.sendVerificationEmail(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.isEmailVerified()) {
            throw new ForbiddenException("Please verify your email before logging in. Check your inbox for the verification link.");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshTokenValue = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshTokenValue, UserResponse.fromEntity(user));
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }

        User user = refreshToken.getUser();

        // Rotate: delete old, create new
        refreshTokenRepository.delete(refreshToken);
        String newRefreshToken = createRefreshToken(user);

        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(accessToken, newRefreshToken, UserResponse.fromEntity(user));
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteByToken(refreshTokenValue);
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token not found in database: {}", token);
                    return new BadRequestException("Invalid or expired verification link.");
                });

        if (vt.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(vt);
            throw new BadRequestException("Verification link has expired. Please request a new one.");
        }

        User user = vt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        verificationTokenRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email."));

        if (user.isEmailVerified()) {
            throw new BadRequestException("This email is already verified. You can log in.");
        }

        emailService.sendVerificationEmail(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());

            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(UUID.randomUUID().toString());
            prt.setUser(user);
            prt.setExpiresAt(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS));
            passwordResetTokenRepository.save(prt);

            emailService.sendPasswordResetEmail(user, prt.getToken());
        });
        // Silently succeed even if email not found (security best practice)
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset link."));

        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(prt);
            throw new BadRequestException("Reset link has expired. Please request a new one.");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.deleteByUserId(user.getId());
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
