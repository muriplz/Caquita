package com.kryeit.auth.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.content.items.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryManager {
    private long user;
    public final Inventory inventory;

    public InventoryManager(long user) {
        this.user = user;
        this.inventory = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM inventories WHERE user_id = :user_id")
                        .bind("user_id", user)
                        .mapTo(Inventory.class)
                        .one()
        );
    }

    public boolean addItem(Item item, int slot) {
        InventoryItem placedItem = calculatePlacement(item, slot);
        if (placedItem == null)
            return false;

        inventory.items().add(placedItem.toJson());
        updateInventory();
        return true;
    }

    public boolean removeItem(InventoryItem item) {
        ArrayNode items = inventory.items();
        String itemId = item.id();

        for (int i = 0; i < items.size(); i++) {
            JsonNode currentItem = items.get(i);
            if (currentItem.get("id").asText().equals(itemId)) {
                items.remove(i);
                break;
            }
        }

        updateInventory();
        return true;
    }

    public boolean moveItem(InventoryItem item, int newSlot) {
        InventoryItem placedItem = calculateMovePlacement(item, newSlot);

        if (placedItem == null)
            return false;

        removeItem(item);
        inventory.items().add(placedItem.toJson());
        updateInventory();
        return true;
    }

    public InventoryItem calculateMovePlacement(InventoryItem existingItem, int newSlot) {
        Item itemType = existingItem.toItem();
        int startX = newSlot % inventory.width();
        int startY = newSlot / inventory.width();

        List<int[]> itemShape = itemType.getShape();

        Set<Integer> newItemCells = new HashSet<>();
        for (int y = 0; y < itemShape.size(); y++) {
            for (int x = 0; x < itemShape.get(y).length; x++) {
                if (itemShape.get(y)[x] == 1) {
                    int cellX = startX + x;
                    int cellY = startY + y;

                    if (cellX >= inventory.width() || cellY >= inventory.height()) {
                        return null;
                    }

                    int cellSlot = cellY * inventory.width() + cellX;
                    newItemCells.add(cellSlot);
                }
            }
        }

        Set<Integer> existingItemCells = new HashSet<>();
        for (int cell : existingItem.cells()) {
            existingItemCells.add(cell);
        }

        ArrayNode items = inventory.items();
        for (int i = 0; i < items.size(); i++) {
            JsonNode itemJson = items.get(i);
            String currentItemId = itemJson.get("id").asText();

            // Skip checking collision with the item being moved
            if (currentItemId.equals(existingItem.id())) {
                continue;
            }

            ArrayNode cellsJson = (ArrayNode) itemJson.get("cells");
            for (int j = 0; j < cellsJson.size(); j++) {
                int cell = cellsJson.get(j).asInt();
                if (newItemCells.contains(cell)) {
                    return null;
                }
            }
        }

        int[] cellsArray = newItemCells.stream().mapToInt(Integer::intValue).toArray();
        return new InventoryItem(itemType.getId(), cellsArray, existingItem.nbt());
    }

    public void updateInventory() {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE inventories SET items = cast(:items as jsonb) WHERE user_id = :user_id")
                    .bind("user_id", user)
                    .bind("items", inventory.items())
                    .execute();
        });
    }

    public InventoryItem calculatePlacement(Item newItem, int slot) {
        int startX = slot % inventory.width();
        int startY = slot / inventory.width();

        List<int[]> newItemShape = newItem.getShape();

        Set<Integer> newItemCells = new HashSet<>();
        for (int y = 0; y < newItemShape.size(); y++) {
            for (int x = 0; x < newItemShape.get(y).length; x++) {
                if (newItemShape.get(y)[x] == 1) {
                    int cellX = startX + x;
                    int cellY = startY + y;

                    if (cellX >= inventory.width() || cellY >= inventory.height()) {
                        return null;
                    }

                    int cellSlot = cellY * inventory.width() + cellX;
                    newItemCells.add(cellSlot);
                }
            }
        }

        ArrayNode items = inventory.items();
        for (int i = 0; i < items.size(); i++) {
            JsonNode itemJson = items.get(i);
            ArrayNode cellsJson = (ArrayNode) itemJson.get("cells");

            for (int j = 0; j < cellsJson.size(); j++) {
                int cell = cellsJson.get(j).asInt();
                if (newItemCells.contains(cell)) {
                    return null;
                }
            }
        }

        int[] cellsArray = newItemCells.stream().mapToInt(Integer::intValue).toArray();
        return new InventoryItem(newItem.getId(), cellsArray, "{}");
    }
}