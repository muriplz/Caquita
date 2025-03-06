package com.kryeit.content.items.plastic;

import com.kryeit.content.items.Item;
import com.kryeit.content.items.Rarity;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class PlasticBottle extends Item {
    private final PlasticClassification plasticType;

    public PlasticBottle() {
        this(PlasticClassification.PET);
    }

    public PlasticBottle(PlasticClassification plasticType) {
        super(
                "plastic:bottle",
                1,
                2,
                Rarity.COMMON,
                ResourceType.PLASTIC,
                createDisposalMap(plasticType),
                createRecyclingReward(plasticType)
        );
        this.plasticType = plasticType;
    }

    public PlasticClassification getPlasticType() {
        return plasticType;
    }

    private static Map<LandmarkType, DisposalOutcome> createDisposalMap(PlasticClassification plasticType) {
        Map<LandmarkType, DisposalOutcome> outcomes = new EnumMap<>(LandmarkType.class);

        // Most plastics go in plastic bin
        outcomes.put(LandmarkType.PLASTIC,
                plasticType.isRecyclable() ? DisposalOutcome.CORRECT : DisposalOutcome.DECENT);

        // Trash can is decent for non-recyclable plastics, otherwise it's wrong
        outcomes.put(LandmarkType.TRASH_CAN,
                plasticType.isRecyclable() ? DisposalOutcome.WRONG : DisposalOutcome.DECENT);

        return outcomes;
    }

    private static RecyclingReward createRecyclingReward(PlasticClassification plasticType) {
        RecyclingReward.Builder builder = RecyclingReward.builder()
                .setDefaultReward(DisposalOutcome.CORRECT, 20)
                .setDefaultReward(DisposalOutcome.DECENT, 5)
                .setDefaultReward(DisposalOutcome.WRONG, -10);

        // PET bottles are worth more when correctly recycled
        if (plasticType == PlasticClassification.PET) {
            builder.addLandmarkReward(LandmarkType.PLASTIC, DisposalOutcome.CORRECT, 25);
        }

        return builder.build();
    }
}