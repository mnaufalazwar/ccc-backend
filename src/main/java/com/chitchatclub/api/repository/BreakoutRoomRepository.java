package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.BreakoutRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface BreakoutRoomRepository extends JpaRepository<BreakoutRoom, UUID> {

    List<BreakoutRoom> findBySessionId(UUID sessionId);

    @Modifying
    @Transactional
    void deleteBySessionId(UUID sessionId);
}
