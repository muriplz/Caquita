package com.kryeit.content.items.glass;

import com.kryeit.content.items.Item;
import com.kryeit.content.items.Rarity;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.Map;

public abstract class GlassItem extends Item {
    private final GlassClassification glassType;

    protected GlassItem(String id, int width, int height, Rarity rarity,
                        GlassClassification glassType,
                        Map<LandmarkType, DisposalOutcome> disposalOutcomes,
                        RecyclingReward recyclingReward) {
        super(
                id,
                width,
                height,
                rarity,
                ResourceType.GLASS,
                disposalOutcomes,
                recyclingReward
        );
        this.glassType = glassType;
    }

    public GlassClassification getGlassType() {
        return glassType;
    }
}