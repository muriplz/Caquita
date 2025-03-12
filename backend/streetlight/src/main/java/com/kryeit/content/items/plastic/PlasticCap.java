package com.kryeit.content.items.plastic;

import com.kryeit.content.items.Rarity;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;

import java.util.EnumMap;
import java.util.Map;

public class PlasticCap extends PlasticItem {

    public PlasticCap() {
        this(PlasticClassification.HDPE);
    }

    public PlasticCap(PlasticClassification plasticType) {
        super(
                "plastic:cap",
                1,
                1,
                Rarity.COMMON,
                plasticType,
                createDisposalMap(plasticType),
                createRecyclingReward(plasticType)
        );
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(PlasticClassification plasticType) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        outcomes.put(LandmarkType.PLASTIC,
                plasticType.isRecyclable() ? DisposalOutcome.CORRECT : DisposalOutcome.DECENT);

        outcomes.put(LandmarkType.TRASH_CAN,
                plasticType.isRecyclable() ? DisposalOutcome.WRONG : DisposalOutcome.DECENT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(PlasticClassification plasticType) {
        RecyclingReward.Builder builder = RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 10)
                .setDefaultReward(DisposalOutcome.DECENT, 2)
                .setDefaultReward(DisposalOutcome.WRONG, -5);

        return builder.build();
    }
}