package com.chitchatclub.api.service;

import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.VerificationToken;
import com.chitchatclub.api.repository.UserRepository;
import com.chitchatclub.api.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private static final Logger log = LoggerFactory.getLogger(VerificationTokenService.class);
    private static final int TOKEN_EXPIRY_HOURS = 48;

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public VerificationTokenService(VerificationTokenRepository tokenRepository,
                                     UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createToken(UUID userId) {
        tokenRepository.deleteByUserId(userId);

        User managedUser = userRepository.getReferenceById(userId);

        VerificationToken vt = new VerificationToken();
        vt.setToken(UUID.randomUUID().toString());
        vt.setUser(managedUser);
        vt.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        tokenRepository.saveAndFlush(vt);
        log.info("Verification token saved — token: {}", vt.getToken());
        return vt.getToken();
    }
}
