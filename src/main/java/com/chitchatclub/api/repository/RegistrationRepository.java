package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    List<Registration> findBySessionId(UUID sessionId);

    Optional<Registration> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId);

    long countBySessionId(UUID sessionId);

    List<Registration> findByUserId(UUID userId);

    List<Registration> findBySessionIdAndAttendedIsNull(UUID sessionId);

    long countByUserIdAndAttendedFalse(UUID userId);
}
