package com.kryeit.content.items;

import com.kryeit.recycling.ResourceType;

import java.util.*;

public interface Item {
    String id();

    List<int[]> shape();

    default Rarity rarity() {
        return Rarity.COMMON;
    }

    ResourceType resourceType();

    default long lifetime() {
        return -1;
    }

    MaterialClassification classification();

    enum Rarity {
        JUNK,
        COMMON,
        UNCOMMON,
        RARE,
        RELIC,
        ;
    }
}
