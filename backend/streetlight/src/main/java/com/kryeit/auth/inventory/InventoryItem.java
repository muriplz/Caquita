package com.kryeit.auth.inventory;

import com.kryeit.content.items.ItemKind;
import com.kryeit.registry.CaquitaItems;

import java.util.List;

public record InventoryItem(
        String id,
        List<Cell> cells,
        Orientation orientation
) {
    public ItemKind toItem() {
        return CaquitaItems.getItem(id);
    }

    public record Cell(int col, int row) {}

    public enum Orientation {
        UP, DOWN, LEFT, RIGHT
        ;

        public Orientation rotate(boolean clockwise) {
            return switch (this) {
                case UP -> clockwise ? RIGHT : LEFT;
                case DOWN -> clockwise ? LEFT : RIGHT;
                case LEFT -> clockwise ? UP : DOWN;
                case RIGHT -> clockwise ? DOWN : UP;
            };
        }
    }
}

