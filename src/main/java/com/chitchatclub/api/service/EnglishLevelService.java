package com.chitchatclub.api.service;

import com.chitchatclub.api.dto.request.UpdateEnglishLevelRequest;
import com.chitchatclub.api.dto.response.EnglishLevelHistoryResponse;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.entity.*;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EnglishLevelService {

    private final UserRepository userRepository;
    private final EnglishLevelHistoryRepository englishLevelHistoryRepository;
    private final BreakoutRoomModeratorRepository breakoutRoomModeratorRepository;
    private final BreakoutRoomMemberRepository breakoutRoomMemberRepository;
    private final RegistrationRepository registrationRepository;

    public EnglishLevelService(UserRepository userRepository,
                               EnglishLevelHistoryRepository englishLevelHistoryRepository,
                               BreakoutRoomModeratorRepository breakoutRoomModeratorRepository,
                               BreakoutRoomMemberRepository breakoutRoomMemberRepository,
                               RegistrationRepository registrationRepository) {
        this.userRepository = userRepository;
        this.englishLevelHistoryRepository = englishLevelHistoryRepository;
        this.breakoutRoomModeratorRepository = breakoutRoomModeratorRepository;
        this.breakoutRoomMemberRepository = breakoutRoomMemberRepository;
        this.registrationRepository = registrationRepository;
    }

    public UserResponse updateLevel(UUID targetUserId, UpdateEnglishLevelRequest request, User changedBy) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        EnglishLevelHistory history = new EnglishLevelHistory();
        history.setUser(target);
        history.setPreviousLevelType(target.getEnglishLevelType());
        history.setPreviousLevelValue(target.getEnglishLevelValue());
        history.setNewLevelType(request.englishLevelType());
        history.setNewLevelValue(request.englishLevelValue());
        history.setChangedBy(changedBy);
        history.setReason(request.reason());
        englishLevelHistoryRepository.save(history);

        target.setEnglishLevelType(request.englishLevelType());
        target.setEnglishLevelValue(request.englishLevelValue());
        target = userRepository.save(target);

        return UserResponse.fromEntity(target, true);
    }

    @Transactional(readOnly = true)
    public boolean canModeratorUpdateUser(UUID moderatorId, UUID targetUserId) {
        List<Registration> moderatorRegistrations = registrationRepository.findByUserId(moderatorId);
        for (Registration reg : moderatorRegistrations) {
            UUID sessionId = reg.getSession().getId();
            if (registrationRepository.existsBySessionIdAndUserId(sessionId, targetUserId)) {
                return true;
            }
        }

        List<BreakoutRoomModerator> roomAssignments = breakoutRoomModeratorRepository.findByUserId(moderatorId);
        for (BreakoutRoomModerator brm : roomAssignments) {
            List<BreakoutRoomMember> members = breakoutRoomMemberRepository.findByBreakoutRoomId(brm.getBreakoutRoom().getId());
            for (BreakoutRoomMember member : members) {
                if (member.getUser().getId().equals(targetUserId)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<EnglishLevelHistoryResponse> getLevelHistory(UUID userId) {
        return englishLevelHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(EnglishLevelHistoryResponse::fromEntity)
                .toList();
    }
}
