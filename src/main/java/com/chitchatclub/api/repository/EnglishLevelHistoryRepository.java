package com.chitchatclub.api.repository;

import com.chitchatclub.api.entity.EnglishLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnglishLevelHistoryRepository extends JpaRepository<EnglishLevelHistory, UUID> {

    List<EnglishLevelHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
