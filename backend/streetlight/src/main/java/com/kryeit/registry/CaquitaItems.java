package com.kryeit.registry;


import com.kryeit.content.items.glass.GlassBottle;
import com.kryeit.content.items.Item;
import com.kryeit.content.items.glass.GlassShard;
import com.kryeit.content.items.plastic.PlasticBottle;
import com.kryeit.content.items.plastic.PlasticCap;
import com.kryeit.content.items.plastic.PlasticTupper;

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
        // GLASS
        register(new GlassBottle());
        register(new GlassShard());

        // PLASTIC
        register(new PlasticBottle());
        register(new PlasticCap());
        register(new PlasticTupper());
    }

    public static void register() {
    }
}