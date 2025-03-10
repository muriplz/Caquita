package com.kryeit.registry;


import com.kryeit.content.items.glass.GlassBottle;
import com.kryeit.content.items.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CaquitaItems {
    private static final Map<String, Item> ITEMS = new HashMap<>();

    public static void register(Item item) {
        ITEMS.put(item.getId(), item);
    }

    public static Item getItem(String id) {
        return ITEMS.get(id);
    }

    public static Collection<Item> getAllItems() {
        return ITEMS.values();
    }

    public static boolean isRegistered(String id) {
        return ITEMS.containsKey(id);
    }

    static {
        register(new GlassBottle());
    }

    public static void register() {
    }
}