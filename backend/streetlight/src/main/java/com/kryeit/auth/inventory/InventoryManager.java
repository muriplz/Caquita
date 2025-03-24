package com.kryeit.auth.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.content.items.Item;

import java.util.*;

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

    public InventoryItem addItem(Item item, int col, int row) {
        InventoryItem placedItem = calculatePlacement(item, col, row);
        if (placedItem == null)
            return null;

        inventory.items().add(placedItem.toJson());
        updateInventory();
        return placedItem;
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

    public InventoryItem moveItem(InventoryItem item, int newCol, int newRow) {
        InventoryItem placedItem = calculateMovePlacement(item, newCol, newRow);

        if (placedItem == null)
            return null;

        removeItem(item);
        inventory.items().add(placedItem.toJson());
        updateInventory();
        return placedItem;
    }

    public InventoryItem calculateMovePlacement(InventoryItem previousInventoryItem, int newCol, int newRow) {
        Item item = previousInventoryItem.toItem();
        ArrayNode previousCells = previousInventoryItem.cells();

        int previousCol = previousInventoryItem.getAnchorCol();
        int previousRow = previousInventoryItem.getAnchorRow();
        
        // Convert previous cells to Cell objects
        List<Cell> previousCellList = new ArrayList<>();
        for (JsonNode cellNode : previousCells) {
            previousCellList.add(Cell.of(cellNode));
        }

        // Calculate offsets
        int colOffset = newCol - previousCol;
        int rowOffset = newRow - previousRow;

        // Create new cells using Cell objects
        List<Cell> newCellList = new ArrayList<>();
        for (Cell cell : previousCellList) {
            Cell newCell = new Cell(cell.col() + colOffset, cell.row() + rowOffset);

            // Check boundaries
            if (newCell.col() < 0 || newCell.col() >= inventory.width() ||
                    newCell.row() < 0 || newCell.row() >= inventory.height()) {
                return null;
            }

            newCellList.add(newCell);
        }

        // Create a set of previous cell positions for quick lookup
        Set<Cell> previousCellPositions = new HashSet<>(previousCellList);

        // First, collect all cells from other items
        Map<Cell, Boolean> otherItemCells = new HashMap<>();
        for (JsonNode inventoryItem : inventory.items()) {
            ArrayNode itemCellsNode = (ArrayNode) inventoryItem.get("cells");

            // Skip if this is the same item we're moving
            if (itemCellsNode.equals(previousCells)) {
                continue;
            }

            // Add all cells from other items
            for (JsonNode cellNode : itemCellsNode) {
                Cell cell = Cell.of(cellNode);
                otherItemCells.put(cell, false);
            }
        }

        // Check for collisions
        boolean hasInvalidCollision = false;

        for (Cell newCell : newCellList) {
            if (otherItemCells.containsKey(newCell)) {
                // If this cell wasn't previously occupied by this item, it's an invalid collision
                if (!previousCellPositions.contains(newCell)) {
                    hasInvalidCollision = true;
                    break;
                }
            }
        }

        // If there are invalid collisions, return null
        if (hasInvalidCollision) {
            return null;
        }

        // Convert new cells back to JsonNode
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode newCellsNode = mapper.createArrayNode();
        for (Cell cell : newCellList) {
            ObjectNode cellNode = mapper.createObjectNode();
            cellNode.put("col", cell.col());
            cellNode.put("row", cell.row());
            newCellsNode.add(cellNode);
        }

        return new InventoryItem(item.getId(), newCellsNode, previousInventoryItem.nbt());
    }

    public void updateInventory() {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE inventories SET items = cast(:items as jsonb) WHERE user_id = :user_id")
                    .bind("user_id", user)
                    .bind("items", inventory.items())
                    .execute();
        });
    }

    public InventoryItem calculatePlacement(Item newItem, int col, int row) {
        List<int[]> newItemShape = newItem.getShape();

        // Create cells for the new item
        List<Cell> newItemCells = new ArrayList<>();
        for (int y = 0; y < newItemShape.size(); y++) {
            for (int x = 0; x < newItemShape.get(y).length; x++) {
                if (newItemShape.get(y)[x] == 1) {
                    int cellCol = col + x;
                    int cellRow = row + y;

                    if (cellCol >= inventory.width() || cellRow >= inventory.height()) {
                        return null;
                    }

                    newItemCells.add(new Cell(cellCol, cellRow));
                }
            }
        }

        // Check for collisions with existing items
        for (JsonNode inventoryItem : inventory.items()) {
            ArrayNode cellsJson = (ArrayNode) inventoryItem.get("cells");

            for (JsonNode cellNode : cellsJson) {
                Cell existingCell = Cell.of(cellNode);

                for (Cell newCell : newItemCells) {
                    if (existingCell.equals(newCell)) {
                        return null;
                    }
                }
            }
        }

        // Convert new cells to JsonNode
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode cellsNode = mapper.createArrayNode();
        for (Cell cell : newItemCells) {
            ObjectNode cellNode = mapper.createObjectNode();
            cellNode.put("col", cell.col());
            cellNode.put("row", cell.row());
            cellsNode.add(cellNode);
        }

        return new InventoryItem(newItem.getId(), cellsNode, "{}");
    }
}