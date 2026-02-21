package com.chitchatclub.api.service;

import com.chitchatclub.api.dto.response.BreakoutRoomResponse;
import com.chitchatclub.api.dto.response.UserResponse;
import com.chitchatclub.api.entity.*;
import com.chitchatclub.api.entity.enums.LevelBucket;
import com.chitchatclub.api.entity.enums.Role;
import com.chitchatclub.api.exception.BadRequestException;
import com.chitchatclub.api.exception.ConflictException;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.*;
import com.chitchatclub.api.util.EnglishLevelNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BreakoutRoomService {

    private final BreakoutRoomRepository breakoutRoomRepository;
    private final BreakoutRoomMemberRepository breakoutRoomMemberRepository;
    private final BreakoutRoomModeratorRepository breakoutRoomModeratorRepository;
    private final RegistrationRepository registrationRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public BreakoutRoomService(BreakoutRoomRepository breakoutRoomRepository,
                               BreakoutRoomMemberRepository breakoutRoomMemberRepository,
                               BreakoutRoomModeratorRepository breakoutRoomModeratorRepository,
                               RegistrationRepository registrationRepository,
                               SessionRepository sessionRepository,
                               UserRepository userRepository) {
        this.breakoutRoomRepository = breakoutRoomRepository;
        this.breakoutRoomMemberRepository = breakoutRoomMemberRepository;
        this.breakoutRoomModeratorRepository = breakoutRoomModeratorRepository;
        this.registrationRepository = registrationRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public List<BreakoutRoomResponse> generateRooms(UUID sessionId, int roomSize) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        breakoutRoomModeratorRepository.deleteByBreakoutRoom_SessionId(sessionId);
        breakoutRoomMemberRepository.deleteByBreakoutRoom_SessionId(sessionId);
        breakoutRoomRepository.deleteBySessionId(sessionId);

        List<Registration> registrations = registrationRepository.findBySessionId(sessionId);

        List<User> moderatorUsers = registrations.stream()
                .filter(Registration::isRegisteredAsModerator)
                .map(Registration::getUser)
                .toList();
        List<User> participantUsers = registrations.stream()
                .filter(r -> !r.isRegisteredAsModerator())
                .map(Registration::getUser)
                .toList();

        int moderatorCount = moderatorUsers.size();
        int participantCapacity = moderatorCount > 0
                ? Math.max(1, roomSize - 1)
                : roomSize;

        Map<LevelBucket, List<User>> usersByBucket = participantUsers.stream()
                .collect(Collectors.groupingBy(EnglishLevelNormalizer::getEffectiveLevel));

        int roomIndexCounter = 1;
        List<BreakoutRoom> allRooms = new ArrayList<>();

        for (Map.Entry<LevelBucket, List<User>> entry : new TreeMap<>(usersByBucket).entrySet()) {
            LevelBucket bucket = entry.getKey();
            List<User> users = entry.getValue();

            List<List<User>> groups = splitIntoGroups(users, participantCapacity);

            for (List<User> group : groups) {
                BreakoutRoom room = new BreakoutRoom();
                room.setSession(session);
                room.setLevelBucket(bucket.name());
                room.setRoomIndex(roomIndexCounter++);
                room = breakoutRoomRepository.save(room);

                for (User user : group) {
                    BreakoutRoomMember member = new BreakoutRoomMember();
                    member.setBreakoutRoom(room);
                    member.setUser(user);
                    breakoutRoomMemberRepository.save(member);
                }

                allRooms.add(room);
            }
        }

        if (!allRooms.isEmpty()) {
            for (int i = 0; i < moderatorUsers.size(); i++) {
                BreakoutRoom targetRoom = allRooms.get(i % allRooms.size());
                BreakoutRoomModerator brm = new BreakoutRoomModerator();
                brm.setBreakoutRoom(targetRoom);
                brm.setUser(moderatorUsers.get(i));
                breakoutRoomModeratorRepository.save(brm);
            }
        }

        return allRooms.stream().map(this::buildRoomResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BreakoutRoomResponse> getRooms(UUID sessionId) {
        List<BreakoutRoom> rooms = breakoutRoomRepository.findBySessionId(sessionId);
        return rooms.stream().map(this::buildRoomResponse).toList();
    }

    public BreakoutRoomResponse addMemberToRoom(UUID roomId, UUID userId) {
        BreakoutRoom room = breakoutRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Breakout room not found with id: " + roomId));

        var userOpt = breakoutRoomMemberRepository.findByBreakoutRoomIdAndUserId(roomId, userId);
        if (userOpt.isPresent()) {
            throw new ConflictException("User is already in this room");
        }

        List<BreakoutRoomMember> existingInSession = breakoutRoomMemberRepository
                .findByBreakoutRoom_SessionId(room.getSession().getId());
        boolean alreadyInAnotherRoom = existingInSession.stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));
        if (alreadyInAnotherRoom) {
            throw new ConflictException("User is already assigned to another room in this session. Use move instead.");
        }

        BreakoutRoomMember member = new BreakoutRoomMember();
        member.setBreakoutRoom(room);
        member.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)));
        breakoutRoomMemberRepository.save(member);

        return buildRoomResponse(room);
    }

    public BreakoutRoomResponse removeMemberFromRoom(UUID roomId, UUID userId) {
        BreakoutRoom room = breakoutRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Breakout room not found with id: " + roomId));

        BreakoutRoomMember member = breakoutRoomMemberRepository.findByBreakoutRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this room"));

        breakoutRoomMemberRepository.delete(member);
        return buildRoomResponse(room);
    }

    public List<BreakoutRoomResponse> moveMemberToRoom(UUID fromRoomId, UUID userId, UUID targetRoomId) {
        if (fromRoomId.equals(targetRoomId)) {
            throw new BadRequestException("Source and target rooms are the same");
        }

        BreakoutRoom fromRoom = breakoutRoomRepository.findById(fromRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Source room not found"));
        BreakoutRoom targetRoom = breakoutRoomRepository.findById(targetRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Target room not found"));

        if (!fromRoom.getSession().getId().equals(targetRoom.getSession().getId())) {
            throw new BadRequestException("Cannot move members between rooms of different sessions");
        }

        BreakoutRoomMember member = breakoutRoomMemberRepository.findByBreakoutRoomIdAndUserId(fromRoomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of the source room"));

        if (breakoutRoomMemberRepository.existsByBreakoutRoomIdAndUserId(targetRoomId, userId)) {
            throw new ConflictException("User is already in the target room");
        }

        breakoutRoomMemberRepository.delete(member);

        BreakoutRoomMember newMember = new BreakoutRoomMember();
        newMember.setBreakoutRoom(targetRoom);
        newMember.setUser(member.getUser());
        breakoutRoomMemberRepository.save(newMember);

        return List.of(buildRoomResponse(fromRoom), buildRoomResponse(targetRoom));
    }

    @Transactional(readOnly = true)
    public String exportRoomsCsv(UUID sessionId) {
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        List<BreakoutRoom> rooms = breakoutRoomRepository.findBySessionId(sessionId);
        StringBuilder csv = new StringBuilder();
        csv.append("Room Index,Level Bucket,Role,Name,Email,English Level Type,English Level Value,Proficiency Level,Override\n");

        for (BreakoutRoom room : rooms) {
            List<BreakoutRoomModerator> roomMods = breakoutRoomModeratorRepository.findByBreakoutRoomId(room.getId());
            List<BreakoutRoomMember> members = breakoutRoomMemberRepository.findByBreakoutRoomId(room.getId());

            for (BreakoutRoomModerator rm : roomMods) {
                User u = rm.getUser();
                LevelBucket effective = EnglishLevelNormalizer.getEffectiveLevel(u);
                String overrideStr = u.getProficiencyLevelOverride() != null ? u.getProficiencyLevelOverride().name() : "";
                csv.append(room.getRoomIndex()).append(',')
                        .append(escapeCsv(room.getLevelBucket())).append(',')
                        .append("Moderator").append(',')
                        .append(escapeCsv(u.getFullName())).append(',')
                        .append(escapeCsv(u.getEmail())).append(',')
                        .append(u.getEnglishLevelType() != null ? u.getEnglishLevelType().name() : "").append(',')
                        .append(u.getEnglishLevelValue() != null ? escapeCsv(u.getEnglishLevelValue()) : "").append(',')
                        .append(effective.getProficiencyLevel()).append(',')
                        .append(overrideStr).append('\n');
            }

            for (BreakoutRoomMember m : members) {
                User u = m.getUser();
                LevelBucket effective = EnglishLevelNormalizer.getEffectiveLevel(u);
                String overrideStr = u.getProficiencyLevelOverride() != null ? u.getProficiencyLevelOverride().name() : "";
                csv.append(room.getRoomIndex()).append(',')
                        .append(escapeCsv(room.getLevelBucket())).append(',')
                        .append("Participant").append(',')
                        .append(escapeCsv(u.getFullName())).append(',')
                        .append(escapeCsv(u.getEmail())).append(',')
                        .append(u.getEnglishLevelType() != null ? u.getEnglishLevelType().name() : "").append(',')
                        .append(u.getEnglishLevelValue() != null ? escapeCsv(u.getEnglishLevelValue()) : "").append(',')
                        .append(effective.getProficiencyLevel()).append(',')
                        .append(overrideStr).append('\n');
            }
        }

        return csv.toString();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getRoomMates(UUID sessionId, UUID userId) {
        Optional<BreakoutRoomMember> membership = breakoutRoomMemberRepository
                .findByBreakoutRoom_SessionIdAndUserId(sessionId, userId);
        if (membership.isEmpty()) {
            return List.of();
        }
        UUID roomId = membership.get().getBreakoutRoom().getId();
        return breakoutRoomMemberRepository.findByBreakoutRoomId(roomId).stream()
                .filter(m -> !m.getUser().getId().equals(userId))
                .map(m -> UserResponse.fromEntity(m.getUser(), false))
                .toList();
    }

    private BreakoutRoomResponse buildRoomResponse(BreakoutRoom room) {
        List<BreakoutRoomMember> members = breakoutRoomMemberRepository.findByBreakoutRoomId(room.getId());
        List<UserResponse> memberResponses = members.stream()
                .map(m -> UserResponse.fromEntity(m.getUser(), true))
                .toList();
        List<BreakoutRoomModerator> roomMods = breakoutRoomModeratorRepository.findByBreakoutRoomId(room.getId());
        List<UserResponse> moderatorResponses = roomMods.stream()
                .map(rm -> UserResponse.fromEntity(rm.getUser(), true))
                .toList();
        return new BreakoutRoomResponse(
                room.getId(),
                room.getLevelBucket(),
                room.getRoomIndex(),
                moderatorResponses,
                memberResponses
        );
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<List<User>> splitIntoGroups(List<User> users, int groupSize) {
        List<List<User>> groups = new ArrayList<>();
        for (int i = 0; i < users.size(); i += groupSize) {
            groups.add(new ArrayList<>(users.subList(i, Math.min(i + groupSize, users.size()))));
        }
        return groups;
    }

    private static boolean isModeratorRole(Role role) {
        return role == Role.MODERATOR || role == Role.ADMIN || role == Role.SUPER_ADMIN;
    }
}
