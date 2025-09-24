package app.caquita.auth.inventory;

import app.caquita.auth.User;
import app.caquita.registry.AllItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryManager {

    private long user;
    public final User.Inventory inventory;

    public InventoryManager(long user) {
        this.user = user;
        this.inventory = InventoryApi.getInventory(user);
    }

    private InventoryItem.Cell getTopLeftOccupied(int[][] shape) {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;

        for (int i = 0; i < shape.length; i++) {
            int[] row = shape[i];
            for (int j = 0; j < row.length; j++) {
                if (row[j] == 1) {
                    minRow = Math.min(minRow, i);
                    minCol = Math.min(minCol, j);
                }
            }
        }

        return new InventoryItem.Cell(minCol, minRow);
    }

    private InventoryItem.Cell anchorToPlacement(int anchorCol, int anchorRow, int[][] shape) {
        InventoryItem.Cell topLeft = getTopLeftOccupied(shape);
        return new InventoryItem.Cell(anchorCol - topLeft.col(), anchorRow - topLeft.row());
    }

    private InventoryItem.Cell getItemAnchor(InventoryItem item) {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;

        for (InventoryItem.Cell cell : item.cells()) {
            minRow = Math.min(minRow, cell.row());
            minCol = Math.min(minCol, cell.col());
        }

        return new InventoryItem.Cell(minCol, minRow);
    }

    public boolean canPlaceItem(String itemId, int anchorCol, int anchorRow) {
        int[][] shape = AllItems.getItem(itemId).getShape();
        InventoryItem.Cell placement = anchorToPlacement(anchorCol, anchorRow, shape);

        int col = placement.col();
        int row = placement.row();

        int shapeHeight = ShapeUtils.getBoundingHeight(shape);
        int shapeWidth  = ShapeUtils.getBoundingWidth(shape);

        if (col < 0 || row < 0
                || col + shapeWidth  > inventory.width()
                || row + shapeHeight > inventory.height()) {
            return false;
        }

        Set<InventoryItem.Cell> occupied = inventory.items().stream()
                .flatMap(item -> item.cells().stream())
                .collect(Collectors.toSet());

        for (int i = 0; i < shapeHeight; i++) {
            int[] shapeRow = shape[i];
            for (int j = 0; j < shapeRow.length; j++) {
                if (shapeRow[j] == 1) {
                    InventoryItem.Cell c = new InventoryItem.Cell(col + j, row + i);
                    if (occupied.contains(c)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean addItem(String itemId, int anchorCol, int anchorRow) {
        int[][] shape = AllItems.getItem(itemId).getShape();
        InventoryItem.Cell placement = anchorToPlacement(anchorCol, anchorRow, shape);

        int col = placement.col();
        int row = placement.row();

        int shapeHeight = ShapeUtils.getBoundingHeight(shape);
        int shapeWidth  = ShapeUtils.getBoundingWidth(shape);

        if (col < 0 || row < 0
                || col + shapeWidth  > inventory.width()
                || row + shapeHeight > inventory.height()) {
            return false;
        }

        Set<InventoryItem.Cell> occupied = inventory.items().stream()
                .flatMap(item -> item.cells().stream())
                .collect(Collectors.toSet());

        List<InventoryItem.Cell> willOccupy = new ArrayList<>();
        for (int i = 0; i < shapeHeight; i++) {
            int[] shapeRow = shape[i];
            for (int j = 0; j < shapeRow.length; j++) {
                if (shapeRow[j] == 1) {
                    InventoryItem.Cell c = new InventoryItem.Cell(col + j, row + i);
                    if (occupied.contains(c)) {
                        return false;
                    }
                    willOccupy.add(c);
                }
            }
        }

        float erre = Math.round(new Random().nextFloat() * 100) / 100f;

        inventory.items().add(
                new InventoryItem(itemId, willOccupy, erre)
        );
        InventoryApi.update(user, inventory.items());
        return true;
    }

    public boolean removeItem(int anchorCol, int anchorRow) {
        InventoryItem item = inventory.items().stream()
                .filter(i -> {
                    InventoryItem.Cell anchor = getItemAnchor(i);
                    return anchor.col() == anchorCol && anchor.row() == anchorRow;
                })
                .findFirst()
                .orElse(null);
        if (item == null) {
            return false;
        }
        inventory.items().remove(item);
        InventoryApi.update(user, inventory.items());
        return true;
    }

    public boolean removeItem(String item, float erre) {
        InventoryItem invItem = inventory.items().stream()
                .filter(i -> i.id().equals(item) && i.erre() == erre)
                .findFirst()
                .orElse(null);
        if (invItem == null) {
            return false;
        }
        inventory.items().remove(invItem);
        InventoryApi.update(user, inventory.items());
        return true;
    }

    public boolean moveItem(int anchorCol, int anchorRow, int newAnchorCol, int newAnchorRow) {
        InventoryItem item = inventory.items().stream()
                .filter(i -> {
                    InventoryItem.Cell anchor = getItemAnchor(i);
                    return anchor.col() == anchorCol && anchor.row() == anchorRow;
                })
                .findFirst()
                .orElse(null);

        if (item == null) return false;

        int[][] shape = item.toItem().getShape();
        InventoryItem.Cell placement = anchorToPlacement(newAnchorCol, newAnchorRow, shape);

        int newCol = placement.col();
        int newRow = placement.row();

        int shapeHeight = ShapeUtils.getBoundingHeight(shape);
        int shapeWidth  = ShapeUtils.getBoundingWidth(shape);

        if (newCol < 0 || newRow < 0
                || newCol + shapeWidth  > inventory.width()
                || newRow + shapeHeight > inventory.height()) {
            return false;
        }

        List<InventoryItem.Cell> willOccupy = ShapeUtils.getOccupied(newCol, newRow, shape);

        List<InventoryItem> collisionedItems = inventory.items().stream()
                .filter(i -> i != item && willOccupy.stream().anyMatch(c -> i.cells().contains(c)))
                .toList();

        if (!collisionedItems.isEmpty()) return false;

        inventory.items().remove(item);
        inventory.items().add(new InventoryItem(item.id(), willOccupy, item.erre()));
        InventoryApi.update(user, inventory.items());
        return true;
    }
}