package com.chitchatclub.api.util;

import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.enums.EnglishLevelType;
import com.chitchatclub.api.entity.enums.LevelBucket;

public final class EnglishLevelNormalizer {

    private EnglishLevelNormalizer() {}

    public static LevelBucket getEffectiveLevel(User user) {
        if (user.getProficiencyLevelOverride() != null) {
            return user.getProficiencyLevelOverride();
        }
        return normalize(user.getEnglishLevelType(), user.getEnglishLevelValue());
    }

    public static LevelBucket normalize(EnglishLevelType type, String value) {
        if (type == null || value == null || value.isBlank()) return LevelBucket.UNSPECIFIED;
        try {
            return switch (type) {
                case IELTS -> normalizeIelts(Double.parseDouble(value.trim()));
                case TOEFL_IBT -> normalizeToeflIbt(Integer.parseInt(value.trim()));
                case TOEFL_ITP -> normalizeToeflItp(Integer.parseInt(value.trim()));
                case DUOLINGO -> normalizeDuolingo(Integer.parseInt(value.trim()));
                case CEFR -> normalizeCefr(value.trim().toUpperCase());
                case OTHER -> LevelBucket.UNSPECIFIED;
            };
        } catch (Exception e) {
            return LevelBucket.UNSPECIFIED;
        }
    }

    private static LevelBucket normalizeIelts(double score) {
        if (score < 0 || score > 9) return LevelBucket.UNSPECIFIED;
        if (score <= 3.5) return LevelBucket.A1;
        if (score <= 4.5) return LevelBucket.A2;
        if (score <= 5.5) return LevelBucket.B1;
        if (score <= 6.5) return LevelBucket.B2;
        if (score <= 8.0) return LevelBucket.C1;
        return LevelBucket.C2;
    }

    private static LevelBucket normalizeToeflIbt(int score) {
        if (score < 0 || score > 120) return LevelBucket.UNSPECIFIED;
        if (score <= 30) return LevelBucket.A1;
        if (score <= 40) return LevelBucket.A2;
        if (score <= 60) return LevelBucket.B1;
        if (score <= 80) return LevelBucket.B2;
        if (score <= 100) return LevelBucket.C1;
        return LevelBucket.C2;
    }

    private static LevelBucket normalizeToeflItp(int score) {
        if (score < 310 || score > 677) return LevelBucket.UNSPECIFIED;
        if (score <= 399) return LevelBucket.A1;
        if (score <= 449) return LevelBucket.A2;
        if (score <= 499) return LevelBucket.B1;
        if (score <= 549) return LevelBucket.B2;
        if (score <= 599) return LevelBucket.C1;
        return LevelBucket.C2;
    }

    private static LevelBucket normalizeDuolingo(int score) {
        if (score < 10 || score > 160) return LevelBucket.UNSPECIFIED;
        if (score <= 55) return LevelBucket.A1;
        if (score <= 85) return LevelBucket.A2;
        if (score <= 110) return LevelBucket.B1;
        if (score <= 130) return LevelBucket.B2;
        if (score <= 150) return LevelBucket.C1;
        return LevelBucket.C2;
    }

    private static LevelBucket normalizeCefr(String value) {
        return switch (value) {
            case "A1" -> LevelBucket.A1;
            case "A2" -> LevelBucket.A2;
            case "B1" -> LevelBucket.B1;
            case "B2" -> LevelBucket.B2;
            case "C1" -> LevelBucket.C1;
            case "C2" -> LevelBucket.C2;
            default -> LevelBucket.UNSPECIFIED;
        };
    }
}
