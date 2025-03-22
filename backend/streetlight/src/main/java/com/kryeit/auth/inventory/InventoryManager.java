package com.kryeit.auth.inventory;

import com.kryeit.content.items.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    private final GridInventory inventory;

    public InventoryManager(GridInventory inventory) {
        this.inventory = inventory;
    }

    public boolean addItem(Item item, Position position) {
        InventoryItem inventoryItem = new InventoryItem(item);
        return addInventoryItem(inventoryItem, position);
    }

    public boolean addInventoryItem(InventoryItem inventoryItem, Position position) {
        if (!canPlaceItem(inventoryItem, position)) {
            return false;
        }

        GridInventory.ItemPlacement placement = new GridInventory.ItemPlacement(inventoryItem, position);
        inventory.itemPlacements().put(inventoryItem.getInstanceId(), placement);
        return true;
    }

    public InventoryItem findItemByIdAndPosition(String itemId, Position position) {
        for (GridInventory.ItemPlacement placement : inventory.itemPlacements().values()) {
            InventoryItem inventoryItem = placement.item();
            Position itemPos = placement.position();

            if (inventoryItem.getItemId().equals(itemId) &&
                    itemPos.x() == position.x() &&
                    itemPos.y() == position.y()) {
                return inventoryItem;
            }
        }

        return null;
    }

    public InventoryItem findItemByInstanceId(String instanceId) {
        GridInventory.ItemPlacement placement = inventory.itemPlacements().get(instanceId);
        return placement != null ? placement.item() : null;
    }

    public boolean removeItem(String instanceId) {
        return inventory.itemPlacements().remove(instanceId) != null;
    }

    public boolean moveItem(String instanceId, Position newPosition) {
        GridInventory.ItemPlacement placement = inventory.itemPlacements().get(instanceId);
        if (placement == null) {
            return false;
        }

        InventoryItem item = placement.item();
        if (!canPlaceItem(item, newPosition, instanceId)) {
            return false;
        }

        GridInventory.ItemPlacement newPlacement = new GridInventory.ItemPlacement(item, newPosition);
        inventory.itemPlacements().put(instanceId, newPlacement);
        return true;
    }

    public boolean canPlaceItem(InventoryItem item, Position position) {
        return canPlaceItem(item, position, null);
    }

    public boolean canPlaceItem(InventoryItem item, Position position, String excludeInstanceId) {
        if (!isPositionValid(position)) {
            return false;
        }

        if (!isItemFitting(item, position)) {
            return false;
        }

        return !isOverlapping(item, position, excludeInstanceId);
    }

    private boolean isPositionValid(Position position) {
        return position.x() >= 0 && position.y() >= 0 &&
                position.x() < inventory.width() && position.y() < inventory.height();
    }

    private boolean isItemFitting(InventoryItem item, Position position) {
        return position.x() + item.getWidth() <= inventory.width() &&
                position.y() + item.getHeight() <= inventory.height();
    }

    private boolean isOverlapping(InventoryItem itemToPlace, Position positionToPlace, String excludeInstanceId) {
        for (Map.Entry<String, GridInventory.ItemPlacement> entry : inventory.itemPlacements().entrySet()) {
            String instanceId = entry.getKey();

            if (excludeInstanceId != null && instanceId.equals(excludeInstanceId)) {
                continue;
            }

            GridInventory.ItemPlacement placement = entry.getValue();
            InventoryItem existingItem = placement.item();
            Position existingPosition = placement.position();

            if (doItemsOverlap(
                    itemToPlace, positionToPlace,
                    existingItem, existingPosition)) {
                return true;
            }
        }

        return false;
    }

    private boolean doItemsOverlap(
            InventoryItem item1, Position pos1,
            InventoryItem item2, Position pos2) {
        return pos1.x() < pos2.x() + item2.getWidth() &&
                pos1.x() + item1.getWidth() > pos2.x() &&
                pos1.y() < pos2.y() + item2.getHeight() &&
                pos1.y() + item1.getHeight() > pos2.y();
    }

    public List<InventoryItem> getItemsAt(Position position) {
        List<InventoryItem> items = new ArrayList<>();

        for (GridInventory.ItemPlacement placement : inventory.itemPlacements().values()) {
            InventoryItem item = placement.item();
            Position itemPos = placement.position();

            if (position.x() >= itemPos.x() && position.x() < itemPos.x() + item.getWidth() &&
                    position.y() >= itemPos.y() && position.y() < itemPos.y() + item.getHeight()) {
                items.add(item);
            }
        }

        return items;
    }

    public Position getItemPosition(String instanceId) {
        GridInventory.ItemPlacement placement = inventory.itemPlacements().get(instanceId);
        return placement != null ? placement.position() : null;
    }

    public GridInventory getInventory() {
        return inventory;
    }
}