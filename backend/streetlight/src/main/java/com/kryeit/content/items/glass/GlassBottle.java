package com.kryeit.content.items.glass;

import com.kryeit.content.items.Rarity;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;

import java.util.EnumMap;
import java.util.Map;

public class GlassBottle extends GlassItem {

    public GlassBottle() {
        this(GlassClassification.CLEAR);
    }

    public GlassBottle(GlassClassification glassType) {
        super(
                "glass:bottle",
                1,
                2,
                Rarity.COMMON,
                glassType,
                createDisposalMap(glassType),
                createRecyclingReward(glassType)
        );
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(GlassClassification glassType) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        outcomes.put(LandmarkType.GLASS,
                glassType.isRecyclable() ? DisposalOutcome.CORRECT : DisposalOutcome.DECENT);

        outcomes.put(LandmarkType.TRASH_CAN,
                glassType.isRecyclable() ? DisposalOutcome.WRONG : DisposalOutcome.DECENT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(GlassClassification glassType) {
        RecyclingReward.Builder builder = RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 15)
                .setDefaultReward(DisposalOutcome.DECENT, 1)
                .setDefaultReward(DisposalOutcome.WRONG, -10);

        if (glassType == GlassClassification.CLEAR) {
            builder.addLandmarkReward(LandmarkType.GLASS, DisposalOutcome.CORRECT, 20);
        }

        builder.addLandmarkReward(LandmarkType.PAPER, DisposalOutcome.WRONG, -15);

        return builder.build();
    }
}