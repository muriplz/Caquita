package com.kryeit.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents user level data with experience and progression information.
 */
public record Level(
        @JsonProperty("level") int level,
        @JsonProperty("experience") int experience,

        @JsonProperty("level-progress")
        int levelProgress,

        @JsonProperty("level-total")
        int levelTotal,

        @JsonProperty("next-level-total")
        int nextLevelTotal
) {
    // Constants
    public static final int LEVEL_1_REQUIRED_EXP = 100;

    public Level() {
        this(0, 0, 0, 0, 0);
    }

    /**
     * Creates a Level object from a total experience value
     */
    public static Level fromExperience(int totalExperience) {
        int level = calculateLevel(totalExperience);
        int expForPreviousLevels = getTotalExpForLevel(level - 1);
        int levelProgress = totalExperience - expForPreviousLevels;
        int levelTotal = getLevelRequiredExp(level - 1);
        int nextLevelTotal = getLevelRequiredExp(level);

        return new Level(level, totalExperience, levelProgress, levelTotal, nextLevelTotal);
    }

    /**
     * Calculate level based on total experience
     */
    private static int calculateLevel(int exp) {
        int level = 0;
        int totalExpRequired = 0;

        while (true) {
            int nextLevelExp = getLevelRequiredExp(level);
            if (exp < totalExpRequired + nextLevelExp) {
                break;
            }
            totalExpRequired += nextLevelExp;
            level++;
        }

        return level;
    }

    /**
     * Get experience required for a specific level
     */
    private static int getLevelRequiredExp(int level) {
        return (int)(LEVEL_1_REQUIRED_EXP * Math.pow(1.1, level));
    }

    /**
     * Get total experience required to reach a specific level
     */
    private static int getTotalExpForLevel(int targetLevel) {
        int totalExp = 0;
        for (int i = 0; i < targetLevel; i++) {
            totalExp += getLevelRequiredExp(i);
        }
        return totalExp;
    }

    /**
     * Get percentage of level completion
     */
    public int getProgressPercentage() {
        return Math.min(100, (int)((levelProgress / (double)levelTotal) * 100));
    }

    /**
     * Get remaining XP needed for next level
     */
    public int getRemainingXp() {
        return levelTotal - levelProgress;
    }

    /**
     * Return trust level based on current level
     */
    public TrustLevel getTrustLevel() {
        return switch (level) {
            case 20, 21, 22, 23, 24 -> TrustLevel.TRUSTED;
            case 25, 26, 27, 28, 29 -> TrustLevel.CONTRIBUTOR;
            case 30, 31, 32, 33, 34, 35 -> TrustLevel.MODERATOR;
            default -> TrustLevel.DEFAULT;
        };
    }
}