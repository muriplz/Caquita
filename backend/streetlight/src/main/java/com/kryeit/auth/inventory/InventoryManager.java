package com.kryeit.auth.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    private final GridInventory inventory;

    public InventoryManager(GridInventory inventory) {
        this.inventory = inventory;
    }

    public boolean addItem(Item item, Position position) {
        if (!canPlaceItem(item, position)) {
            return false;
        }

        inventory.itemPositions().put(item, position);
        return true;
    }

    public boolean removeItem(Item item) {
        return inventory.itemPositions().remove(item) != null;
    }

    public boolean moveItem(Item item, Position newPosition) {
        if (!inventory.itemPositions().containsKey(item)) {
            return false;
        }

        if (!canPlaceItem(item, newPosition)) {
            return false;
        }

        inventory.itemPositions().put(item, newPosition);
        return true;
    }

    public boolean canPlaceItem(Item item, Position position) {
        if (!isPositionValid(position)) {
            return false;
        }

        if (!isItemFitting(item, position)) {
            return false;
        }

        return !isOverlapping(item, position);
    }

    private boolean isPositionValid(Position position) {
        return position.x() >= 0 && position.y() >= 0 &&
                position.x() < inventory.width() && position.y() < inventory.height();
    }

    private boolean isItemFitting(Item item, Position position) {
        return position.x() + item.getWidth() <= inventory.width() &&
                position.y() + item.getHeight() <= inventory.height();
    }

    private boolean isOverlapping(Item itemToPlace, Position positionToPlace) {
        for (Map.Entry<Item, Position> entry : inventory.itemPositions().entrySet()) {
            Item existingItem = entry.getKey();

            if (existingItem.equals(itemToPlace)) {
                continue;
            }

            Position existingPosition = entry.getValue();

            if (doItemsOverlap(
                    itemToPlace, positionToPlace,
                    existingItem, existingPosition)) {
                return true;
            }
        }

        return false;
    }

    private boolean doItemsOverlap(
            Item item1, Position pos1,
            Item item2, Position pos2) {
        return pos1.x() < pos2.x() + item2.getWidth() &&
                pos1.x() + item1.getWidth() > pos2.x() &&
                pos1.y() < pos2.y() + item2.getHeight() &&
                pos1.y() + item1.getHeight() > pos2.y();
    }

    public List<Item> getItemsAt(Position position) {
        List<Item> items = new ArrayList<>();

        for (Map.Entry<Item, Position> entry : inventory.itemPositions().entrySet()) {
            Item item = entry.getKey();
            Position itemPos = entry.getValue();

            if (position.x() >= itemPos.x() && position.x() < itemPos.x() + item.getWidth() &&
                    position.y() >= itemPos.y() && position.y() < itemPos.y() + item.getHeight()) {
                items.add(item);
            }
        }

        return items;
    }

    public Position getItemPosition(Item item) {
        return inventory.itemPositions().get(item);
    }

    public GridInventory getInventory() {
        return inventory;
    }
}