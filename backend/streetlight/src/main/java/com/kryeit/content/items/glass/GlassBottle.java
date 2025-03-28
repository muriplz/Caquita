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

public class GlassBottle extends ResourceItem<GlassClassification> {

    public GlassBottle() {
        this(GlassClassification.CLEAR);
    }

    public GlassBottle(GlassClassification classification) {
        super(
                "glass:bottle",
                List.of(
                        new int[]{1, 0},
                        new int[]{1, 0}
                ),
                Rarity.COMMON,
                ResourceType.GLASS,
                classification,
                createDisposalMap(classification),
                createRecyclingReward(classification),
                "{}"
        );
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(GlassClassification classification) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        outcomes.put(LandmarkType.GLASS,
                classification.isRecyclable() ? DisposalOutcome.CORRECT : DisposalOutcome.DECENT);

        outcomes.put(LandmarkType.TRASH_CAN,
                classification.isRecyclable() ? DisposalOutcome.WRONG : DisposalOutcome.DECENT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(GlassClassification classification) {
        RecyclingReward.Builder builder = RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 15)
                .setDefaultReward(DisposalOutcome.DECENT, 1)
                .setDefaultReward(DisposalOutcome.WRONG, -10);

        if (classification == GlassClassification.CLEAR) {
            builder.addLandmarkReward(LandmarkType.GLASS, DisposalOutcome.CORRECT, 20);
        }

        builder.addLandmarkReward(LandmarkType.PAPER, DisposalOutcome.WRONG, -15);

        return builder.build();
    }
}