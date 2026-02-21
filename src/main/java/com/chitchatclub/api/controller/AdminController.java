package com.chitchatclub.api.controller;

import com.chitchatclub.api.dto.request.CreateSessionRequest;
import com.chitchatclub.api.dto.request.MoveRoomMemberRequest;
import com.chitchatclub.api.dto.request.RoomMemberRequest;
import com.chitchatclub.api.dto.request.UpdateProficiencyOverrideRequest;
import com.chitchatclub.api.dto.request.UpdateSessionRequest;
import com.chitchatclub.api.dto.response.BreakoutRoomResponse;
import com.chitchatclub.api.dto.response.FeedbackResponse;
import com.chitchatclub.api.dto.response.RegistrationResponse;
import com.chitchatclub.api.dto.response.SessionResponse;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.UserRepository;
import com.chitchatclub.api.service.AttendanceService;
import com.chitchatclub.api.service.BreakoutRoomService;
import com.chitchatclub.api.service.FeedbackService;
import com.chitchatclub.api.service.SessionService;
import com.chitchatclub.api.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {

    private final SessionService sessionService;
    private final BreakoutRoomService breakoutRoomService;
    private final FeedbackService feedbackService;
    private final UserService userService;
    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public AdminController(SessionService sessionService,
                           BreakoutRoomService breakoutRoomService,
                           FeedbackService feedbackService,
                           UserService userService,
                           AttendanceService attendanceService,
                           UserRepository userRepository) {
        this.sessionService = sessionService;
        this.breakoutRoomService = breakoutRoomService;
        this.feedbackService = feedbackService;
        this.userService = userService;
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessionsForAdmin());
    }

    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request,
                                                          Authentication authentication) {
        User creator = resolveUser(authentication);
        SessionResponse response = sessionService.createSession(request, creator);
        return ResponseEntity.ok(SessionResponse.fromEntity(sessionService.getSessionEntity(response.id()), 0, true));
    }

    @PatchMapping("/sessions/{id}")
    public ResponseEntity<SessionResponse> updateSession(@PathVariable UUID id,
                                                          @RequestBody UpdateSessionRequest request) {
        return ResponseEntity.ok(sessionService.getSessionByIdForAdmin(
                sessionService.updateSession(id, request).id()));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<SessionResponse> getSessionForAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.getSessionByIdForAdmin(id));
    }

    @GetMapping("/sessions/{id}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistrations(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.getRegistrations(id));
    }

    @PostMapping("/sessions/{id}/generate-rooms")
    public ResponseEntity<List<BreakoutRoomResponse>> generateRooms(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "4") int roomSize) {
        return ResponseEntity.ok(breakoutRoomService.generateRooms(id, roomSize));
    }

    @GetMapping("/sessions/{id}/rooms")
    public ResponseEntity<List<BreakoutRoomResponse>> getRooms(@PathVariable UUID id) {
        return ResponseEntity.ok(breakoutRoomService.getRooms(id));
    }

    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<BreakoutRoomResponse> addRoomMember(@PathVariable UUID roomId,
                                                                @Valid @RequestBody RoomMemberRequest request) {
        return ResponseEntity.ok(breakoutRoomService.addMemberToRoom(roomId, request.userId()));
    }

    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public ResponseEntity<BreakoutRoomResponse> removeRoomMember(@PathVariable UUID roomId,
                                                                   @PathVariable UUID userId) {
        return ResponseEntity.ok(breakoutRoomService.removeMemberFromRoom(roomId, userId));
    }

    @PostMapping("/rooms/{roomId}/members/{userId}/move")
    public ResponseEntity<List<BreakoutRoomResponse>> moveRoomMember(@PathVariable UUID roomId,
                                                                       @PathVariable UUID userId,
                                                                       @Valid @RequestBody MoveRoomMemberRequest request) {
        return ResponseEntity.ok(breakoutRoomService.moveMemberToRoom(roomId, userId, request.targetRoomId()));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String email) {
        List<User> users = userRepository.findByEmailContainingIgnoreCase(email);
        return ResponseEntity.ok(users.stream()
                .map(u -> UserResponse.fromEntity(u, true))
                .toList());
    }

    @PatchMapping("/users/{id}/proficiency-level")
    public ResponseEntity<UserResponse> updateProficiencyOverride(@PathVariable UUID id,
                                                                    @RequestBody UpdateProficiencyOverrideRequest request) {
        User target = userService.getUserById(id);
        target.setProficiencyLevelOverride(request.proficiencyLevel());
        target = userRepository.save(target);
        return ResponseEntity.ok(UserResponse.fromEntity(target, true));
    }

    @GetMapping("/sessions/{id}/rooms/export")
    public ResponseEntity<byte[]> exportRoomsCsv(@PathVariable UUID id) {
        String csv = breakoutRoomService.exportRoomsCsv(id);
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=breakout-rooms.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    @GetMapping("/sessions/{id}/feedback")
    public ResponseEntity<List<FeedbackResponse>> getSessionFeedback(@PathVariable UUID id) {
        return ResponseEntity.ok(feedbackService.getSessionFeedback(id));
    }

    @PostMapping("/sessions/{id}/finalize-attendance")
    public ResponseEntity<Void> finalizeAttendance(@PathVariable UUID id) {
        attendanceService.finalizeAttendance(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new java.util.HashMap<>();
        config.put("max_no_shows", String.valueOf(attendanceService.getConfigInt("max_no_shows", 3)));
        config.put("blacklist_duration_days", String.valueOf(attendanceService.getConfigInt("blacklist_duration_days", 30)));
        config.put("unregister_cutoff_hours", String.valueOf(attendanceService.getConfigInt("unregister_cutoff_hours", 24)));
        return ResponseEntity.ok(config);
    }

    @PatchMapping("/config")
    public ResponseEntity<Void> updateConfig(@RequestBody Map<String, String> updates) {
        updates.forEach(attendanceService::setConfig);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/no-shows")
    public ResponseEntity<List<UserResponse>> getUsersWithNoShows() {
        return ResponseEntity.ok(
            userRepository.findByNoShowCountGreaterThanOrderByNoShowCountDesc(0).stream()
                    .map(u -> UserResponse.fromEntity(u, true))
                    .toList()
        );
    }

    @GetMapping("/users/blacklisted")
    public ResponseEntity<List<UserResponse>> getBlacklistedUsers() {
        return ResponseEntity.ok(
            userRepository.findByBlacklistedUntilAfter(java.time.LocalDateTime.now()).stream()
                    .map(u -> UserResponse.fromEntity(u, true))
                    .toList()
        );
    }

    @PostMapping("/users/{id}/whitelist")
    public ResponseEntity<UserResponse> whitelistUser(@PathVariable UUID id) {
        attendanceService.whitelistUser(id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.fromEntity(user, true));
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
