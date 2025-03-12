package com.kryeit.content.items.plastic;

import com.kryeit.content.items.Item;
import com.kryeit.content.items.Rarity;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.Map;

public abstract class PlasticItem extends Item {
    private final PlasticClassification plasticType;

    protected PlasticItem(String id, int width, int height, Rarity rarity,
                          PlasticClassification plasticType,
                          Map<LandmarkType, DisposalOutcome> disposalOutcomes,
                          RecyclingReward recyclingReward) {
        super(
                id,
                width,
                height,
                rarity,
                ResourceType.PLASTIC,
                disposalOutcomes,
                recyclingReward
        );
        this.plasticType = plasticType;
    }

    public PlasticClassification getPlasticType() {
        return plasticType;
    }
}