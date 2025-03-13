package com.kryeit.content.items;

import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.Map;

public abstract class ResourceItem<T extends Enum<T>> extends Item {
    private final T classification;

    protected ResourceItem(String id, int width, int height, Rarity rarity,
                           ResourceType resourceType, T classification,
                           Map<LandmarkType, DisposalOutcome> disposalOutcomes,
                           RecyclingReward recyclingReward) {
        super(
                id,
                width,
                height,
                rarity,
                resourceType,
                disposalOutcomes,
                recyclingReward
        );
        this.classification = classification;
    }

    public T getType() {
        return classification;
    }
}