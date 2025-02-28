package com.kryeit.auth.inventory;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

public record GridInventory(long id, long userId, int width, int height, Map<Item, Position> itemPositions) {
    public GridInventory(long id, long userId, int width, int height) {
        this(id, userId, width, height, new HashMap<>());
    }

    @ConstructorProperties({"id", "userId", "width", "height", "itemPositions"})
    public GridInventory {
        if (itemPositions == null) {
            itemPositions = new HashMap<>();
        }
    }
}