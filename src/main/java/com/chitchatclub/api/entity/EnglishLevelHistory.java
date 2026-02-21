package com.chitchatclub.api.entity;

import com.chitchatclub.api.entity.enums.EnglishLevelType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "english_level_history")
public class EnglishLevelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private EnglishLevelType previousLevelType;

    private String previousLevelValue;

    @Enumerated(EnumType.STRING)
    private EnglishLevelType newLevelType;

    private String newLevelValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    private String reason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public EnglishLevelHistory() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EnglishLevelType getPreviousLevelType() {
        return previousLevelType;
    }

    public void setPreviousLevelType(EnglishLevelType previousLevelType) {
        this.previousLevelType = previousLevelType;
    }

    public String getPreviousLevelValue() {
        return previousLevelValue;
    }

    public void setPreviousLevelValue(String previousLevelValue) {
        this.previousLevelValue = previousLevelValue;
    }

    public EnglishLevelType getNewLevelType() {
        return newLevelType;
    }

    public void setNewLevelType(EnglishLevelType newLevelType) {
        this.newLevelType = newLevelType;
    }

    public String getNewLevelValue() {
        return newLevelValue;
    }

    public void setNewLevelValue(String newLevelValue) {
        this.newLevelValue = newLevelValue;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
