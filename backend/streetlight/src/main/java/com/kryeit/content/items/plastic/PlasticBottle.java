package com.kryeit.content.items.plastic;

import com.kryeit.content.items.Rarity;
import com.kryeit.content.items.ResourceItem;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlasticBottle extends ResourceItem<PlasticClassification> {

    public PlasticBottle() {
        this(PlasticClassification.PET);
    }

    public PlasticBottle(PlasticClassification classification) {
        super(
                "plastic:bottle",
                List.of(
                        new int[]{1, 0},
                        new int[]{1, 0}
                ),
                Rarity.COMMON,
                ResourceType.PLASTIC,
                classification,
                createDisposalMap(classification),
                createRecyclingReward(classification),
                "{}"
        );
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(PlasticClassification classification) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        outcomes.put(LandmarkType.PLASTIC,
                classification.isRecyclable() ? DisposalOutcome.CORRECT : DisposalOutcome.DECENT);

        outcomes.put(LandmarkType.TRASH_CAN,
                classification.isRecyclable() ? DisposalOutcome.WRONG : DisposalOutcome.DECENT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(PlasticClassification classification) {
        RecyclingReward.Builder builder = RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 20)
                .setDefaultReward(DisposalOutcome.DECENT, 5)
                .setDefaultReward(DisposalOutcome.WRONG, -10);

        return builder.build();
    }
}