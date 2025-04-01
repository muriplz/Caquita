package com.kryeit.landmark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LandmarkFeatureDefinitions {
    private static final Map<LandmarkType, Set<String>> VALID_FEATURES = new HashMap<>();

    static {
        VALID_FEATURES.put(LandmarkType.TRASH_CAN,
                new HashSet<>(Arrays.asList("ashtray", "inundated", "windblown", "overwhelmed")));

        VALID_FEATURES.put(LandmarkType.PLASTIC_CONTAINER,
                new HashSet<>(Arrays.asList("underground", "bottlenecked", "modern", "overwhelmed")));
    }

    public static Set<String> getValidFeatures(LandmarkType type) {
        return VALID_FEATURES.getOrDefault(type, new HashSet<>());
    }

    public static boolean isValidFeature(LandmarkType type, String feature, String level) {
        boolean isValidLevel = Level.isValid(level);
        if (!isValidLevel) return false;
        return VALID_FEATURES.getOrDefault(type, new HashSet<>()).contains(feature);
    }

    public enum Level {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        ;

        public static boolean isValid(String level) {
            if (level == null) return false;

            try {
                valueOf(level.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        public static Level fromString(String level) {
            if (level == null) return NONE;

            try {
                return valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                return NONE;
            }
        }
    }
}