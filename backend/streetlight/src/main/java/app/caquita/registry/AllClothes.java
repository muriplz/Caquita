package app.caquita.registry;


import app.caquita.auth.inventory.clothes.Clothe;
import app.caquita.auth.inventory.clothes.dusters.Duster;
import app.caquita.content.items.ItemKind;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static app.caquita.registry.AllItems.ITEMS;

public class AllClothes {
    private static final Map<String, Clothe> CLOTHES = new HashMap<>();

    public static void register(Clothe clothe, ItemKind item) {
        CLOTHES.put(clothe.getId(), clothe);
        ITEMS.put(clothe.getId(), item);
    }

    public static Clothe getClothe(String id) {
        return CLOTHES.get(id);
    }

    public static Collection<Clothe> getAllClothes() {
        return CLOTHES.values();
    }

    public static boolean isDuster(String id) {
        Clothe clothe = getClothe(id);
        return clothe instanceof Duster;
    }

    public static boolean isRegistered(String id) {
        return CLOTHES.containsKey(id);
    }

    static {
    }

    public static void register() {
    }
}