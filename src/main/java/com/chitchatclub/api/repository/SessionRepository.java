package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.Session;
import com.chitchatclub.api.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByStatus(SessionStatus status);

    List<Session> findByStatusAndStartDateTimeAfter(SessionStatus status, LocalDateTime after);

    List<Session> findByStatusAndStartDateTimeAfterOrderByStartDateTimeAsc(SessionStatus status, LocalDateTime after);
}
