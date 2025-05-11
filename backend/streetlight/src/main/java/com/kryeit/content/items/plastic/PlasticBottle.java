package com.kryeit.content.items.plastic;

import com.kryeit.content.items.Item;
import com.kryeit.content.items.MaterialClassification;
import com.kryeit.content.items.classifications.PlasticClassification;
import com.kryeit.recycling.ResourceType;

import java.util.List;

public class PlasticBottle implements Item {

    @Override
    public String id() {
        return "plastic:bottle";
    }

    @Override
    public List<int[]> shape() {
        return List.of(
                new int[]{1, 0},
                new int[]{1, 0}
        );
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.PLASTIC;
    }

    @Override
    public MaterialClassification classification() {
        return PlasticClassification.PET;
    }
}