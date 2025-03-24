package com.kryeit.content.items.metal;

import com.kryeit.content.items.Rarity;
import com.kryeit.content.items.ResourceItem;
import com.kryeit.content.items.glass.GlassClassification;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MetalChains extends ResourceItem<MetalClassification> {

    public MetalChains() {
        this(MetalClassification.STEEL);
    }

    public MetalChains(MetalClassification classification) {
        super(
                "metal:chains",
                List.of(
                        new int[]{0, 1, 1},
                        new int[]{1, 1, 0},
                        new int[]{0, 0, 0}
                ),
                Rarity.COMMON,
                ResourceType.METAL,
                classification,
                createDisposalMap(classification),
                createRecyclingReward(classification),
                "{}"
        );
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(MetalClassification classification) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        outcomes.put(LandmarkType.METAL,
                classification.isRecyclable() ? DisposalOutcome.CORRECT : DisposalOutcome.DECENT);

        outcomes.put(LandmarkType.TRASH_CAN,
                classification.isRecyclable() ? DisposalOutcome.WRONG : DisposalOutcome.DECENT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(MetalClassification classification) {
        RecyclingReward.Builder builder = RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 15)
                .setDefaultReward(DisposalOutcome.DECENT, 1)
                .setDefaultReward(DisposalOutcome.WRONG, -10);

        builder.addLandmarkReward(LandmarkType.PAPER, DisposalOutcome.WRONG, -15);

        return builder.build();
    }
}