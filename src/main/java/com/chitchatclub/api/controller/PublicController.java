package com.chitchatclub.api.controller;

import com.chitchatclub.api.dto.response.SessionResponse;
import com.chitchatclub.api.service.SessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public")
public class PublicController {

    private final SessionService sessionService;

    public PublicController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/sessions/upcoming")
    public ResponseEntity<List<SessionResponse>> getUpcomingSessions() {
        return ResponseEntity.ok(sessionService.getUpcomingSessions());
    }
}
