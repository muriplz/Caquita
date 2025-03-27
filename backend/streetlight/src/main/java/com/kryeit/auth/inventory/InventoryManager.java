package com.kryeit.auth.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.content.items.Item;

import java.util.ArrayList;
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

    public InventoryItem addItem(Item item, int col, int row) {
        InventoryItem newItem = new InventoryItem(item.getId(), null, Orientation.UP, "{}");
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

    public InventoryItem rotateItem(InventoryItem item, boolean clockwise, int heldCol, int heldRow) {

        // Create a new orientation based on rotation direction
        Orientation newOrientation = item.orientation().rotate(clockwise);

        // Get the item from registry to get its shape
        Item itemObject = item.toItem();
        if (itemObject == null) {
            return null;
        }

        // Get the current cells
        List<Cell> currentCells = new ArrayList<>();
        ArrayNode cellsArray = item.cells();
        for (JsonNode cellNode : cellsArray) {
            currentCells.add(Cell.of(cellNode));
        }

        // Find the cell closest to the held position (this will be our pivot)
        Cell pivotCell = findNearestCell(currentCells, heldCol, heldRow);

        // Calculate new cells after rotation around the pivot
        List<Cell> newCells = rotateAroundPivot(currentCells, pivotCell, clockwise);

        // Check that all new cells are within boundaries
        for (Cell cell : newCells) {
            if (cell.col() < 0 || cell.col() >= inventory.width() ||
                    cell.row() < 0 || cell.row() >= inventory.height()) {
                return null;
            }
        }

        // Check for collisions with other items
        Set<Cell> newCellSet = new HashSet<>(newCells);
        for (JsonNode inventoryItem : inventory.items()) {
            String id = inventoryItem.get("id").asText();

            // Skip the item we're rotating
            if (id.equals(item.id())) {
                continue;
            }

            ArrayNode cellsNode = (ArrayNode) inventoryItem.get("cells");
            for (JsonNode cellNode : cellsNode) {
                Cell cell = Cell.of(cellNode);
                if (newCellSet.contains(cell)) {
                    return null;
                }
            }
        }

        // Create JSON for new cells
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode cellsNode = mapper.createArrayNode();
        for (Cell cell : newCells) {
            ObjectNode cellNode = mapper.createObjectNode();
            cellNode.put("col", cell.col());
            cellNode.put("row", cell.row());
            cellsNode.add(cellNode);
        }

        // Create the rotated item
        InventoryItem rotatedItem = new InventoryItem(item.id(), cellsNode, newOrientation, item.nbt());

        // Update inventory
        removeItem(item);
        inventory.items().add(rotatedItem.toJson());
        updateInventory();

        return rotatedItem;
    }

    private Cell findNearestCell(List<Cell> cells, int heldCol, int heldRow) {
        Cell closest = cells.get(0);
        int minDistance = Integer.MAX_VALUE;

        for (Cell cell : cells) {
            int dx = cell.col() - heldCol;
            int dy = cell.row() - heldRow;
            int distance = dx * dx + dy * dy;  // squared distance is fine for comparison

            if (distance < minDistance) {
                minDistance = distance;
                closest = cell;
            }
        }

        return closest;
    }

    private List<Cell> rotateAroundPivot(List<Cell> cells, Cell pivot, boolean clockwise) {
        List<Cell> rotatedCells = new ArrayList<>();

        for (Cell cell : cells) {
            // Translate cell coordinates to be relative to pivot
            int relX = cell.col() - pivot.col();
            int relY = cell.row() - pivot.row();

            // Rotate by 90 degrees around origin (0,0)
            int newRelX, newRelY;
            if (clockwise) {
                newRelX = relY;
                newRelY = -relX;
            } else {
                newRelX = -relY;
                newRelY = relX;
            }

            // Translate back to original coordinate system
            int newCol = pivot.col() + newRelX;
            int newRow = pivot.row() + newRelY;

            rotatedCells.add(new Cell(newCol, newRow));
        }

        return rotatedCells;
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

        return new InventoryItem(item.getId(), newCellsNode, previousInventoryItem.orientation(), previousInventoryItem.nbt());
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
        Item item = newItem.toItem();
        List<int[]> itemShape = getTransformedShape(item.getShape(), newItem.orientation());

        // Create cells for the new item
        List<Cell> newItemCells = new ArrayList<>();
        for (int y = 0; y < itemShape.size(); y++) {
            for (int x = 0; x < itemShape.get(y).length; x++) {
                if (itemShape.get(y)[x] == 1) {
                    int cellCol = col + x;
                    int cellRow = row + y;

                    if (cellCol < 0 || cellCol >= inventory.width() ||
                            cellRow < 0 || cellRow >= inventory.height()) {
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
            if (itemId.equals(newItem.id()) && inventoryItem.has("cells") &&
                    newItem.cells() != null && inventoryItem.get("cells").equals(newItem.cells())) {
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

        return new InventoryItem(newItem.id(), cellsNode, newItem.orientation(), newItem.nbt());
    }

    private List<int[]> getTransformedShape(List<int[]> originalShape, Orientation orientation) {
        if (orientation == Orientation.UP) {
            return originalShape;
        }

        int height = originalShape.size();
        int width = height > 0 ? originalShape.get(0).length : 0;

        List<int[]> transformedShape = new ArrayList<>();

        switch (orientation) {
            case DOWN:
                // Rotate 180 degrees
                for (int y = 0; y < height; y++) {
                    int[] row = new int[width];
                    for (int x = 0; x < width; x++) {
                        row[x] = originalShape.get(height - 1 - y)[width - 1 - x];
                    }
                    transformedShape.add(row);
                }
                break;

            case LEFT:
                // Rotate 90 degrees counterclockwise
                for (int x = width - 1; x >= 0; x--) {
                    int[] row = new int[height];
                    for (int y = 0; y < height; y++) {
                        row[y] = originalShape.get(y)[x];
                    }
                    transformedShape.add(row);
                }
                break;

            case RIGHT:
                // Rotate 90 degrees clockwise
                for (int x = 0; x < width; x++) {
                    int[] row = new int[height];
                    for (int y = 0; y < height; y++) {
                        row[y] = originalShape.get(height - 1 - y)[x];
                    }
                    transformedShape.add(row);
                }
                break;

            default:
                return originalShape;
        }

        return transformedShape;
    }
}