package app.caquita.auth.inventory;

import app.caquita.content.items.ItemKind;
import app.caquita.registry.AllItems;

import java.util.List;

public record InventoryItem(
        String id,
        List<Cell> cells,
        float erre
) {
    public ItemKind toItem() {
        return AllItems.getItem(id);
    }

    public record Cell(int col, int row) {}
}