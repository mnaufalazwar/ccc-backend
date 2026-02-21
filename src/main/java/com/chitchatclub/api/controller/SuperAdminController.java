package com.chitchatclub.api.controller;

import com.chitchatclub.api.dto.request.ChangeRoleRequest;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin")
@Tag(name = "SuperAdmin")
public class SuperAdminController {

    private final UserService userService;

    public SuperAdminController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable UUID id,
                                                    @Valid @RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(userService.changeRole(id, request.role()));
    }
}
