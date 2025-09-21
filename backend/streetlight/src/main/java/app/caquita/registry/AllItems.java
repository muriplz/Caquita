package app.caquita.registry;


import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ToolItemKind;
import app.caquita.content.items.glass.GlassBottle;
import app.caquita.content.items.plastic.Bottle;
import app.caquita.content.items.plastic.Pipe;
import app.caquita.content.items.plastic.tools.Fork;

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

    public static boolean isTool(String id) {
        ItemKind item = getItem(id);
        return item instanceof ToolItemKind;
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


        // TOOLS
        register(new Fork());
    }

    public static void register() {
    }
}