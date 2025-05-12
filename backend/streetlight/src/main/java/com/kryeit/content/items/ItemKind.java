package com.kryeit.content.items;

import java.util.List;

public interface ItemKind {
    String getId();

    List<int[]> getShape();

    default Rarity getRarity() {
        return Rarity.COMMON;
    }

    ResourceType getResourceType();

    String getClassification();

    enum Rarity {
        JUNK,
        COMMON,
        UNCOMMON,
        RARE,
        RELIC,
        ;
    }
}
