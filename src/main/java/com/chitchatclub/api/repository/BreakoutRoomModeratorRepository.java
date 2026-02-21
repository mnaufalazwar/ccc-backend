package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.BreakoutRoomModerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface BreakoutRoomModeratorRepository extends JpaRepository<BreakoutRoomModerator, UUID> {

    List<BreakoutRoomModerator> findByBreakoutRoomId(UUID roomId);

    List<BreakoutRoomModerator> findByUserId(UUID userId);

    @Modifying
    @Transactional
    void deleteByBreakoutRoom_SessionId(UUID sessionId);
}
