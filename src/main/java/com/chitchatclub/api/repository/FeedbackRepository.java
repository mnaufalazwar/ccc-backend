package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findBySessionIdAndFromUserId(UUID sessionId, UUID fromUserId);

    List<Feedback> findBySessionIdAndToUserId(UUID sessionId, UUID toUserId);

    List<Feedback> findBySessionId(UUID sessionId);

    List<Feedback> findBySessionIdAndToUserIsNull(UUID sessionId);
}
