package com.chitchatclub.api.entity;

import com.chitchatclub.api.entity.enums.EnglishLevelType;
import com.chitchatclub.api.entity.enums.LevelBucket;
import com.chitchatclub.api.entity.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role = Role.PARTICIPANT;

    @Enumerated(EnumType.STRING)
    private EnglishLevelType englishLevelType;

    private String englishLevelValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level_override")
    private LevelBucket proficiencyLevelOverride;

    private boolean emailVerified = false;

    private int noShowCount = 0;

    private LocalDateTime blacklistedUntil;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public EnglishLevelType getEnglishLevelType() {
        return englishLevelType;
    }

    public void setEnglishLevelType(EnglishLevelType englishLevelType) {
        this.englishLevelType = englishLevelType;
    }

    public String getEnglishLevelValue() {
        return englishLevelValue;
    }

    public void setEnglishLevelValue(String englishLevelValue) {
        this.englishLevelValue = englishLevelValue;
    }

    public LevelBucket getProficiencyLevelOverride() {
        return proficiencyLevelOverride;
    }

    public void setProficiencyLevelOverride(LevelBucket proficiencyLevelOverride) {
        this.proficiencyLevelOverride = proficiencyLevelOverride;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public int getNoShowCount() {
        return noShowCount;
    }

    public void setNoShowCount(int noShowCount) {
        this.noShowCount = noShowCount;
    }

    public LocalDateTime getBlacklistedUntil() {
        return blacklistedUntil;
    }

    public void setBlacklistedUntil(LocalDateTime blacklistedUntil) {
        this.blacklistedUntil = blacklistedUntil;
    }
}
