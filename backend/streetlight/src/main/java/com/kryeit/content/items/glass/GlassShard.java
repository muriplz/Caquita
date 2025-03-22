package com.kryeit.content.items.glass;

import com.kryeit.content.items.Rarity;
import com.kryeit.content.items.ResourceItem;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GlassShard extends ResourceItem<GlassClassification> {

    public GlassShard() {
        this(GlassClassification.MIXED);
    }

    public GlassShard(GlassClassification glassType) {
        super(
                "glass:shard",
                List.of(
                        new int[]{1}
                ),
                Rarity.JUNK,
                ResourceType.GLASS,
                glassType,
                createDisposalMap(glassType),
                createRecyclingReward(glassType),
                "{}"
        );
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(GlassClassification glassType) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        outcomes.put(LandmarkType.GLASS, DisposalOutcome.DECENT);
        outcomes.put(LandmarkType.TRASH_CAN, DisposalOutcome.CORRECT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(GlassClassification glassType) {
        return RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 5)
                .setDefaultReward(DisposalOutcome.DECENT, 1)
                .setDefaultReward(DisposalOutcome.WRONG, -5)
                .build();
    }
}