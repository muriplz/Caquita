package com.kryeit.recycling;

import com.kryeit.landmark.LandmarkType;

import java.util.EnumMap;
import java.util.Map;

public class RecyclingReward {
    private final Map<DisposalOutcome, Integer> defaultRewards;
    private final Map<LandmarkType, Map<DisposalOutcome, Integer>> landmarkSpecificRewards;

    private RecyclingReward(Map<DisposalOutcome, Integer> defaultRewards,
                            Map<LandmarkType, Map<DisposalOutcome, Integer>> landmarkSpecificRewards) {
        this.defaultRewards = new EnumMap<>(defaultRewards);
        this.landmarkSpecificRewards = new EnumMap<>(landmarkSpecificRewards);
    }

    public int getReward(LandmarkType landmarkType, DisposalOutcome outcome) {
        // First check if there's a specific reward for this landmark/outcome
        if (landmarkSpecificRewards.containsKey(landmarkType) &&
                landmarkSpecificRewards.get(landmarkType).containsKey(outcome)) {
            return landmarkSpecificRewards.get(landmarkType).get(outcome);
        }

        // Otherwise use the default reward for this outcome
        return defaultRewards.getOrDefault(outcome, 0);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<DisposalOutcome, Integer> defaultRewards = new EnumMap<>(DisposalOutcome.class);
        private final Map<LandmarkType, Map<DisposalOutcome, Integer>> landmarkSpecificRewards =
                new EnumMap<>(LandmarkType.class);

        public Builder setDefaultReward(DisposalOutcome outcome, int expValue) {
            validateReward(outcome, expValue);
            defaultRewards.put(outcome, expValue);
            return this;
        }

        public Builder addLandmarkReward(LandmarkType landmarkType, DisposalOutcome outcome, int expValue) {
            validateReward(outcome, expValue);
            landmarkSpecificRewards
                    .computeIfAbsent(landmarkType, k -> new EnumMap<>(DisposalOutcome.class))
                    .put(outcome, expValue);
            return this;
        }

        private void validateReward(DisposalOutcome outcome, int expValue) {
            if (outcome == DisposalOutcome.WRONG && expValue > 0) {
                throw new IllegalArgumentException("Wrong disposal reward must be negative or zero");
            }
        }

        public RecyclingReward build() {
            // Ensure defaults are set
            if (!defaultRewards.containsKey(DisposalOutcome.CORRECT)) {
                defaultRewards.put(DisposalOutcome.CORRECT, 10);
            }
            if (!defaultRewards.containsKey(DisposalOutcome.DECENT)) {
                defaultRewards.put(DisposalOutcome.DECENT, 2);
            }
            if (!defaultRewards.containsKey(DisposalOutcome.WRONG)) {
                defaultRewards.put(DisposalOutcome.WRONG, -5);
            }

            return new RecyclingReward(defaultRewards, landmarkSpecificRewards);
        }
    }
}