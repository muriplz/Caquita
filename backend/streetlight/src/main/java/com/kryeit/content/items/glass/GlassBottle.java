package com.kryeit.content.items.glass;

import com.kryeit.content.items.Item;
import com.kryeit.content.items.Rarity;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class GlassBottle extends Item {
    private final String type;

    public GlassBottle() {
        super(
                "glass:bottle",
                1,
                2,
                Rarity.COMMON,
                ResourceType.GLASS,
                createDisposalMap(),
                createRecyclingReward()
        );
        this.type = "transparent";
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap() {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);
        outcomes.put(LandmarkType.GLASS, DisposalOutcome.CORRECT);
        outcomes.put(LandmarkType.TRASH_CAN, DisposalOutcome.DECENT);
        return outcomes;
    }

    private static RecyclingReward createRecyclingReward() {
        return RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 15)
                .setDefaultReward(DisposalOutcome.DECENT, 1)
                .setDefaultReward(DisposalOutcome.WRONG, -10)

                .addLandmarkReward(LandmarkType.PAPER, DisposalOutcome.WRONG, -15)
                .build();
    }

    public String getType() {
        return type;
    }
}