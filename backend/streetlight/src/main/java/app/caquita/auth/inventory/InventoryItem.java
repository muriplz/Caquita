package app.caquita.auth.inventory;

import app.caquita.content.items.ItemKind;
import app.caquita.registry.AllItems;

import java.util.List;

public record InventoryItem(
        String id,
        List<Cell> cells,
        Orientation orientation,
        float erre
) {
    public ItemKind toItem() {
        return AllItems.getItem(id);
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

