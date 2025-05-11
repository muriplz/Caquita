package com.kryeit.registry;


import com.kryeit.content.items.Item;
import com.kryeit.content.items.plastic.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CaquitaItems {
    private static final Map<String, Item> ITEMS = new HashMap<>();

    public static void register(Item item) {
        ITEMS.put(item.id(), item);
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
        // PLASTIC
        register(new PlasticBottle());
    }

    public static void register() {
    }
}