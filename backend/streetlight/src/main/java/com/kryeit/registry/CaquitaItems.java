package com.kryeit.registry;


import com.kryeit.content.items.ItemKind;
import com.kryeit.content.items.glass.GlassBottle;
import com.kryeit.content.items.plastic.PlasticBottle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CaquitaItems {
    private static final Map<String, ItemKind> ITEMS = new HashMap<>();

    public static void register(ItemKind item) {
        ITEMS.put(item.getId(), item);
    }

    public static ItemKind getItem(String id) {
        return ITEMS.get(id);
    }

    public static Collection<ItemKind> getAllItems() {
        return ITEMS.values();
    }

    public static boolean isRegistered(String id) {
        return ITEMS.containsKey(id);
    }

    static {
        // PLASTIC
        register(new PlasticBottle());

        // GLASS
        register(new GlassBottle());
    }

    public static void register() {
    }
}