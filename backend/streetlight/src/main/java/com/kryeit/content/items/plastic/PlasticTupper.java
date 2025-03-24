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

public class PlasticTupper extends ResourceItem<PlasticClassification> {

    public PlasticTupper() {
        this(PlasticClassification.PP);
    }

    public PlasticTupper(PlasticClassification classification) {
        super(
                "plastic:tupper",
                List.of(
                        new int[]{1, 1},
                        new int[]{1, 1}
                ),
                Rarity.UNCOMMON,
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
                .setDefaultReward(DisposalOutcome.CORRECT, 25)
                .setDefaultReward(DisposalOutcome.DECENT, 8)
                .setDefaultReward(DisposalOutcome.WRONG, -15);

        return builder.build();
    }
}