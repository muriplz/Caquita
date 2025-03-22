package com.kryeit.auth.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kryeit.Database;
import com.kryeit.content.items.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Example inventory.items() json
// [
//   {
//     "id": "plastic:bottle",
//     "cells": [0, 3],
//     "nbt": "{}"
//   },
//   {
//     ...
//   }
// ]
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
        inventory.items().remove(item.toJson());
        updateInventory();
        return true;
    }

    public boolean moveItem(InventoryItem item, int newSlot) {
        InventoryItem placedItem = calculatePlacement(item.toItem(), newSlot);

        if (placedItem == null)
            return false;

        inventory.items().remove(item.toJson());
        inventory.items().add(placedItem.toJson());
        updateInventory();
        return true;
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

        // Get the shape of the new item
        List<int[]> newItemShape = newItem.getShape();

        // Calculate all cells the new item would occupy
        Set<Integer> newItemCells = new HashSet<>();
        for (int y = 0; y < newItemShape.size(); y++) {
            for (int x = 0; x < newItemShape.get(y).length; x++) {
                if (newItemShape.get(y)[x] == 1) {
                    int cellX = startX + x;
                    int cellY = startY + y;

                    // Check if the item would go outside the inventory bounds
                    if (cellX >= inventory.width() || cellY >= inventory.height()) {
                        return null;
                    }

                    int cellSlot = cellY * inventory.width() + cellX;
                    newItemCells.add(cellSlot);
                }
            }
        }

        // Check for collisions with existing items in the inventory
        JsonArray items = inventory.items();
        for (int i = 0; i < items.size(); i++) {
            JsonObject itemJson = items.get(i).getAsJsonObject();
            JsonArray cellsJson = itemJson.get("cells").getAsJsonArray();

            for (int j = 0; j < cellsJson.size(); j++) {
                int cell = cellsJson.get(j).getAsInt();
                if (newItemCells.contains(cell)) {
                    // Found a collision
                    return null;
                }
            }
        }

        // Convert Set to int array for InventoryItem
        int[] cellsArray = newItemCells.stream().mapToInt(Integer::intValue).toArray();

        // Create new InventoryItem with calculated cells
        return new InventoryItem(newItem.getId(), cellsArray, "{}");
    }

}