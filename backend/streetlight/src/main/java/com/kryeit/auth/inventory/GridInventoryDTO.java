package com.kryeit.auth.inventory;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

public record GridInventoryDTO(long id, long userId, int width, int height, String itemPositions) {
    public GridInventoryDTO(long id, long userId, int width, int height) {
        this(id, userId, width, height, "");
    }

    @ConstructorProperties({"id", "userId", "width", "height", "itemPositions"})
    public GridInventoryDTO {
        if (itemPositions == null) {
            itemPositions = "";
        }
    }
}