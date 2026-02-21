package com.chitchatclub.api.controller;

import com.chitchatclub.api.dto.request.UpdateEnglishLevelRequest;
import com.chitchatclub.api.dto.response.BreakoutRoomResponse;
import com.chitchatclub.api.dto.response.EnglishLevelHistoryResponse;
import com.chitchatclub.api.dto.response.SessionResponse;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.enums.Role;
import com.chitchatclub.api.exception.ForbiddenException;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.RegistrationRepository;
import com.chitchatclub.api.repository.UserRepository;
import com.chitchatclub.api.service.BreakoutRoomService;
import com.chitchatclub.api.service.EnglishLevelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/moderator")
@Tag(name = "Moderator")
public class ModeratorController {

    private final EnglishLevelService englishLevelService;
    private final BreakoutRoomService breakoutRoomService;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public ModeratorController(EnglishLevelService englishLevelService,
                               BreakoutRoomService breakoutRoomService,
                               RegistrationRepository registrationRepository,
                               UserRepository userRepository) {
        this.englishLevelService = englishLevelService;
        this.breakoutRoomService = breakoutRoomService;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @PatchMapping("/users/{id}/english-level")
    public ResponseEntity<UserResponse> updateEnglishLevel(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnglishLevelRequest request,
            Authentication authentication) {

        User currentUser = resolveUser(authentication);

        boolean isAdminOrAbove = currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.SUPER_ADMIN;

        if (!isAdminOrAbove) {
            if (!englishLevelService.canModeratorUpdateUser(currentUser.getId(), id)) {
                throw new ForbiddenException("You do not have access to update this user's English level");
            }
        }

        return ResponseEntity.ok(englishLevelService.updateLevel(id, request, currentUser));
    }

    @GetMapping("/users/{id}/english-level-history")
    public ResponseEntity<List<EnglishLevelHistoryResponse>> getLevelHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(englishLevelService.getLevelHistory(id));
    }

    @GetMapping("/my-sessions")
    public ResponseEntity<List<SessionResponse>> getModeratorSessions(Authentication authentication) {
        User currentUser = resolveUser(authentication);
        List<SessionResponse> sessions = registrationRepository.findByUserId(currentUser.getId()).stream()
                .map(reg -> {
                    var session = reg.getSession();
                    long count = registrationRepository.countBySessionId(session.getId());
                    return SessionResponse.fromEntity(session, count);
                })
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{id}/rooms")
    public ResponseEntity<List<BreakoutRoomResponse>> getRooms(@PathVariable UUID id,
                                                                Authentication authentication) {
        User currentUser = resolveUser(authentication);
        boolean isRegistered = registrationRepository.existsBySessionIdAndUserId(id, currentUser.getId());
        boolean isAdminOrAbove = currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.SUPER_ADMIN;
        if (!isRegistered && !isAdminOrAbove) {
            throw new ForbiddenException("You are not registered for this session");
        }
        return ResponseEntity.ok(breakoutRoomService.getRooms(id));
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
