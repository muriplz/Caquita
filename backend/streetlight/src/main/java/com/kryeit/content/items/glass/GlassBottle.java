package com.kryeit.content.items.glass;

import com.kryeit.content.items.ItemKind;
import com.kryeit.content.items.ResourceType;
import com.kryeit.content.items.classifications.GlassClassification;

import java.util.List;

public class GlassBottle implements ItemKind {

    @Override
    public String getId() {
        return "glass:bottle";
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
        return ResourceType.GLASS;
    }

    @Override
    public String getClassification() {
        return String.valueOf(GlassClassification.TEMPERED);
    }
}