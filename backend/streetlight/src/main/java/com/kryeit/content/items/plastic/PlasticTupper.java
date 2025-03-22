package com.kryeit.content.items.plastic;

import com.kryeit.content.items.Rarity;
import com.kryeit.content.items.ResourceItem;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class PlasticTupper extends ResourceItem<PlasticClassification> {

    public PlasticTupper() {
        this(PlasticClassification.PP);
    }

    public PlasticTupper(PlasticClassification plasticType) {
        super(
                "plastic:tupper",
                2,
                2,
                Rarity.UNCOMMON,
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
                .setDefaultReward(DisposalOutcome.CORRECT, 25)
                .setDefaultReward(DisposalOutcome.DECENT, 8)
                .setDefaultReward(DisposalOutcome.WRONG, -15);

        return builder.build();
    }
}