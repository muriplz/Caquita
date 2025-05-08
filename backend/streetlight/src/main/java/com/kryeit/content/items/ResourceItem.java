package com.kryeit.content.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.List;
import java.util.Map;

public abstract class ResourceItem<T extends Enum<T> & MaterialClassification> extends Item {
    private final T classification;

    protected ResourceItem(String id, List<int[]> shape, Rarity rarity,
                           ResourceType resourceType, T classification,
                           Map<LandmarkType, DisposalOutcome> disposalOutcomes,
                           RecyclingReward recyclingReward, String nbt) {
        super(
                id,
                shape,
                rarity,
                resourceType,
                disposalOutcomes,
                recyclingReward,
                nbt
        );
        this.classification = classification;
    }

    public T getType() {
        return classification;
    }

    @JsonProperty("classification")
    public T getClassification() {
        return classification;
    }
}