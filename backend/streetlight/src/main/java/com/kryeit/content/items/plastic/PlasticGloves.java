package com.kryeit.content.items.plastic;

import com.kryeit.content.items.Rarity;
import com.kryeit.content.items.ResourceItem;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class PlasticGloves extends ResourceItem<PlasticClassification> {

    public PlasticGloves() {
        this(PlasticClassification.LDPE);
    }

    public PlasticGloves(PlasticClassification plasticType) {
        super(
                "plastic:gloves",
                2,
                1,
                Rarity.RARE,
                ResourceType.PLASTIC,
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
                .setDefaultReward(DisposalOutcome.CORRECT, 15)
                .setDefaultReward(DisposalOutcome.DECENT, 3)
                .setDefaultReward(DisposalOutcome.WRONG, -8);

        return builder.build();
    }
}