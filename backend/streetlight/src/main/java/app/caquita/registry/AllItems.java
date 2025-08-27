package app.caquita.registry;


import app.caquita.content.items.ItemKind;
import app.caquita.content.items.glass.GlassBottle;
import app.caquita.content.items.plastic.Bottle;
import app.caquita.content.items.plastic.Pipe;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AllItems {
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
        register(new Bottle());
        register(new Pipe());

        // GLASS
        register(new GlassBottle());
    }

    public static void register() {
    }
}