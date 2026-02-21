package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.BreakoutRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BreakoutRoomMemberRepository extends JpaRepository<BreakoutRoomMember, UUID> {

    List<BreakoutRoomMember> findByBreakoutRoomId(UUID roomId);

    List<BreakoutRoomMember> findByBreakoutRoom_SessionId(UUID sessionId);

    Optional<BreakoutRoomMember> findByBreakoutRoomIdAndUserId(UUID roomId, UUID userId);

    boolean existsByBreakoutRoomIdAndUserId(UUID roomId, UUID userId);

    Optional<BreakoutRoomMember> findByBreakoutRoom_SessionIdAndUserId(UUID sessionId, UUID userId);

    @Modifying
    @Transactional
    void deleteByBreakoutRoom_SessionId(UUID sessionId);
}
