package com.kryeit.landmark.forum.petitions;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kryeit.landmark.LandmarkFeatureDefinitions;
import com.kryeit.landmark.LandmarkType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PetitionLandmarkInfo(
        @JsonProperty("name") String name
) {
    private static final Map<String, String> featureLevels = new HashMap<>();

    @JsonAnySetter
    public void setFeature(String key, String value) {
        if (!key.equals("name")) {
            featureLevels.put(key, value);
        }
    }

    public static boolean validateLevel(String feature, String level) {
        List<LandmarkFeatureDefinitions.Level> levels = Arrays.stream(LandmarkFeatureDefinitions.Level.values()).toList();

        if (featureLevels.containsKey(feature)) {
            return levels.indexOf(LandmarkFeatureDefinitions.Level.valueOf(featureLevels.get(feature))) >= levels.indexOf(LandmarkFeatureDefinitions.Level.valueOf(level));
        }

        return false;
    }

    public Map<String, String> getFeatureLevels() {
        return featureLevels;
    }

    public String getFeatureLevel(String feature) {
        return featureLevels.get(feature);
    }
}
