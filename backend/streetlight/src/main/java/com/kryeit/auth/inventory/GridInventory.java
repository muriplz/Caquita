package com.kryeit.auth.inventory;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

public record GridInventory(long id, long userId, int width, int height, Map<String, ItemPlacement> itemPlacements) {
    public GridInventory(long id, long userId, int width, int height) {
        this(id, userId, width, height, new HashMap<>());
    }

    @ConstructorProperties({"id", "userId", "width", "height", "itemPlacements"})
    public GridInventory {
        if (itemPlacements == null) {
            itemPlacements = new HashMap<>();
        }
    }

    public record ItemPlacement(InventoryItem item, Position position) {}
}