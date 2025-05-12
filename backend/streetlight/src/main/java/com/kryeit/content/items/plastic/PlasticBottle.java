package com.kryeit.content.items.plastic;

import com.kryeit.content.items.ItemKind;
import com.kryeit.content.items.ResourceType;
import com.kryeit.content.items.classifications.PlasticClassification;

import java.util.List;

public class PlasticBottle implements ItemKind {

    @Override
    public String getId() {
        return "plastic:bottle";
    }

    @Override
    public List<int[]> getShape() {
        return List.of(
                new int[]{1, 0},
                new int[]{1, 0}
        );
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PLASTIC;
    }

    @Override
    public String getClassification() {
        return String.valueOf(PlasticClassification.PET.code);
    }
}