package com.kryeit.content.items.cardboard;

import com.kryeit.content.items.Rarity;
import com.kryeit.content.items.ResourceItem;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PizzaBox extends ResourceItem<CardboardClassification> {

    public PizzaBox() {
        this(CardboardClassification.CORRUGATED);
    }

    public PizzaBox(CardboardClassification classification) {
        super(
                "cardboard:pizza_box",
                List.of(
                        new int[]{0, 1},
                        new int[]{1, 1}
                ),
                Rarity.UNCOMMON,
                ResourceType.CARDBOARD,
                classification,
                createDisposalMap(classification),
                createRecyclingReward(classification),
                "{}"
        );
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(CardboardClassification classification) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        outcomes.put(LandmarkType.PLASTIC,
                classification.isRecyclable() ? DisposalOutcome.CORRECT : DisposalOutcome.DECENT);

        outcomes.put(LandmarkType.TRASH_CAN,
                classification.isRecyclable() ? DisposalOutcome.WRONG : DisposalOutcome.DECENT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(CardboardClassification classification) {
        RecyclingReward.Builder builder = RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 20)
                .setDefaultReward(DisposalOutcome.DECENT, 5)
                .setDefaultReward(DisposalOutcome.WRONG, -10);

        return builder.build();
    }
}