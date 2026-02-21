package com.chitchatclub.api.service;

import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.enums.Role;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponse getUserResponse(UUID id) {
        return UserResponse.fromEntity(getUserById(id), true);
    }

    public UserResponse changeRole(UUID userId, Role newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        user = userRepository.save(user);
        return UserResponse.fromEntity(user, true);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
