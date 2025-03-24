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
        InventoryItem newItem = new InventoryItem(item.getId(), null, "{}");
        InventoryItem placedItem = calculatePlacement(newItem, col, row);
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

        List<Cell> previousCellList = new ArrayList<>();
        for (JsonNode cellNode : previousCells) {
            previousCellList.add(Cell.of(cellNode));
        }

        int colOffset = newCol - previousCol;
        int rowOffset = newRow - previousRow;

        List<Cell> newCellList = new ArrayList<>();
        for (Cell cell : previousCellList) {
            Cell newCell = new Cell(cell.col() + colOffset, cell.row() + rowOffset);

            if (newCell.col() < 0 || newCell.col() >= inventory.width() ||
                    newCell.row() < 0 || newCell.row() >= inventory.height()) {
                return null;
            }

            newCellList.add(newCell);
        }

        Set<Cell> newCellSet = new HashSet<>(newCellList);

        for (JsonNode inventoryItem : inventory.items()) {
            ArrayNode itemCellsNode = (ArrayNode) inventoryItem.get("cells");

            if (itemCellsNode.equals(previousCells)) {
                continue;
            }

            for (JsonNode cellNode : itemCellsNode) {
                Cell cell = Cell.of(cellNode);
                if (newCellSet.contains(cell)) {
                    return null;
                }
            }
        }

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

    public InventoryItem calculatePlacement(InventoryItem newItem, int col, int row) {
        List<int[]> newItemShape = newItem.toItem().getShape();

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

        // Convert new cells to a set for quick lookups
        Set<Cell> newCellSet = new HashSet<>(newItemCells);

        // Check for collisions with existing items
        for (JsonNode inventoryItem : inventory.items()) {
            String itemId = inventoryItem.get("id").asText();

            // If this is the same item we're replacing, skip collision check
            if (itemId.equals(newItem.id())) {
                continue;
            }

            ArrayNode cellsJson = (ArrayNode) inventoryItem.get("cells");

            for (JsonNode cellNode : cellsJson) {
                Cell existingCell = Cell.of(cellNode);

                if (newCellSet.contains(existingCell)) {
                    return null;
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

        return new InventoryItem(newItem.id(), cellsNode, "{}");
    }
}