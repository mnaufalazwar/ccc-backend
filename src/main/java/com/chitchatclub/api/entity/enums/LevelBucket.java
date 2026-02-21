package com.chitchatclub.api.entity.enums;

public enum LevelBucket {
    A1("Beginner"),
    A2("Elementary"),
    B1("Intermediate"),
    B2("Upper Intermediate"),
    C1("Advanced"),
    C2("Proficient"),
    UNSPECIFIED("Not determined");

    private final String proficiencyLevel;

    LevelBucket(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }
}
