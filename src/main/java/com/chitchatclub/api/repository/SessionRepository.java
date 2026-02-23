package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.Session;
import com.chitchatclub.api.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByStatus(SessionStatus status);

    List<Session> findByStatusAndStartDateTimeAfter(SessionStatus status, Instant after);

    List<Session> findByStatusAndStartDateTimeAfterOrderByStartDateTimeAsc(SessionStatus status, Instant after);
}
