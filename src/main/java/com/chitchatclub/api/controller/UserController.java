package com.chitchatclub.api.controller;

import com.chitchatclub.api.dto.request.ChangePasswordRequest;
import com.chitchatclub.api.dto.request.UpdateMyEnglishLevelRequest;
import com.chitchatclub.api.dto.response.SessionResponse;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.enums.Role;
import com.chitchatclub.api.exception.ForbiddenException;
import com.chitchatclub.api.repository.UserRepository;
import com.chitchatclub.api.service.AuthService;
import com.chitchatclub.api.service.SessionService;
import com.chitchatclub.api.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final AuthService authService;

    public UserController(UserService userService,
                          SessionService sessionService,
                          UserRepository userRepository,
                          AuthService authService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        User user = resolveUser(authentication);
        return ResponseEntity.ok(UserResponse.fromEntity(user, isAdminOrAbove(user), true));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id, Authentication authentication) {
        User requester = resolveUser(authentication);
        boolean admin = isAdminOrAbove(requester);

        if (!admin && !requester.getId().equals(id)) {
            throw new ForbiddenException("You can only view your own profile");
        }

        User target = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.fromEntity(target, admin));
    }

    @PatchMapping("/me/english-level")
    public ResponseEntity<UserResponse> updateMyEnglishLevel(@Valid @RequestBody UpdateMyEnglishLevelRequest request,
                                                              Authentication authentication) {
        User user = resolveUser(authentication);
        user.setEnglishLevelType(request.englishLevelType());
        user.setEnglishLevelValue(request.englishLevelValue());
        user.setProficiencyLevelOverride(null);
        user = userRepository.save(user);
        return ResponseEntity.ok(UserResponse.fromEntity(user, isAdminOrAbove(user)));
    }

    private boolean isAdminOrAbove(User user) {
        return user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN;
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                               Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    @GetMapping("/me/sessions")
    public ResponseEntity<List<SessionResponse>> getMyRegisteredSessions(Authentication authentication) {
        User user = resolveUser(authentication);
        return ResponseEntity.ok(sessionService.getMyRegisteredSessions(user.getId()));
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new com.chitchatclub.api.exception.ResourceNotFoundException("User not found"));
    }
}
