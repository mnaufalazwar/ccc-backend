package com.chitchatclub.api.controller;

import com.chitchatclub.api.dto.request.FeedbackRequest;
import com.chitchatclub.api.dto.request.VerifyAttendanceRequest;
import com.chitchatclub.api.dto.response.FeedbackResponse;
import com.chitchatclub.api.dto.response.RegistrationResponse;
import com.chitchatclub.api.dto.response.SessionResponse;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.repository.AppConfigRepository;
import com.chitchatclub.api.repository.RegistrationRepository;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.UserRepository;
import com.chitchatclub.api.service.AttendanceService;
import com.chitchatclub.api.service.BreakoutRoomService;
import com.chitchatclub.api.service.FeedbackService;
import com.chitchatclub.api.service.SessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions")
public class SessionController {

    private final SessionService sessionService;
    private final FeedbackService feedbackService;
    private final AttendanceService attendanceService;
    private final BreakoutRoomService breakoutRoomService;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final AppConfigRepository appConfigRepository;

    public SessionController(SessionService sessionService,
                             FeedbackService feedbackService,
                             AttendanceService attendanceService,
                             BreakoutRoomService breakoutRoomService,
                             RegistrationRepository registrationRepository,
                             UserRepository userRepository,
                             AppConfigRepository appConfigRepository) {
        this.sessionService = sessionService;
        this.feedbackService = feedbackService;
        this.attendanceService = attendanceService;
        this.breakoutRoomService = breakoutRoomService;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.appConfigRepository = appConfigRepository;
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getOpenSessions() {
        return ResponseEntity.ok(sessionService.getOpenSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<RegistrationResponse> registerForSession(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean asModerator,
            Authentication authentication) {
        User user = resolveUser(authentication);
        return ResponseEntity.ok(sessionService.registerUser(id, user, asModerator));
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<Void> unregisterFromSession(@PathVariable UUID id,
                                                       Authentication authentication) {
        User user = resolveUser(authentication);
        sessionService.unregisterUser(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<FeedbackResponse> submitFeedback(@PathVariable UUID id,
                                                            @Valid @RequestBody FeedbackRequest request,
                                                            Authentication authentication) {
        User user = resolveUser(authentication);
        return ResponseEntity.ok(feedbackService.submitFeedback(id, user, request));
    }

    @GetMapping("/{id}/feedback/me")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(@PathVariable UUID id,
                                                                 Authentication authentication) {
        User user = resolveUser(authentication);
        return ResponseEntity.ok(feedbackService.getMyFeedback(id, user.getId()));
    }

    @GetMapping("/{id}/my-registration")
    public ResponseEntity<RegistrationResponse> getMyRegistration(@PathVariable UUID id,
                                                                    Authentication authentication) {
        User user = resolveUser(authentication);
        return registrationRepository.findBySessionIdAndUserId(id, user.getId())
                .map(reg -> ResponseEntity.ok(RegistrationResponse.fromEntity(reg)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/feedback/received")
    public ResponseEntity<List<FeedbackResponse>> getReceivedFeedback(@PathVariable UUID id,
                                                                       Authentication authentication) {
        User user = resolveUser(authentication);
        return ResponseEntity.ok(feedbackService.getReceivedFeedback(id, user.getId()));
    }

    @GetMapping("/{id}/my-room-members")
    public ResponseEntity<List<UserResponse>> getMyRoomMembers(@PathVariable UUID id,
                                                                Authentication authentication) {
        User user = resolveUser(authentication);
        return ResponseEntity.ok(breakoutRoomService.getRoomMates(id, user.getId()));
    }

    @PostMapping("/{id}/verify-attendance")
    public ResponseEntity<Map<String, Object>> verifyAttendance(@PathVariable UUID id,
                                                                  @Valid @RequestBody VerifyAttendanceRequest request,
                                                                  Authentication authentication) {
        User user = resolveUser(authentication);
        boolean verified = attendanceService.verifyAttendance(id, user, request.attendanceCode());
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getPublicConfig() {
        int cutoffHours = appConfigRepository.findById("unregister_cutoff_hours")
                .map(c -> { try { return Integer.parseInt(c.getConfigValue()); } catch (NumberFormatException e) { return 24; } })
                .orElse(24);
        return ResponseEntity.ok(Map.of("unregisterCutoffHours", cutoffHours));
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
