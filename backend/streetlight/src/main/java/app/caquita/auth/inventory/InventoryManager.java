package app.caquita.auth.inventory;

import app.caquita.auth.User;
import app.caquita.content.items.ShapeUtils;
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

    public boolean canPlaceItem(String itemId, int col, int row) {
        List<int[]> shape = AllItems.getItem(itemId).getShape();

        int shapeHeight = ShapeUtils.getBoundingHeight(shape, InventoryItem.Orientation.UP);
        int shapeWidth  = ShapeUtils.getBoundingWidth(shape, InventoryItem.Orientation.UP);

        if (col < 0 || row < 0
                || col + shapeWidth  > inventory.width()
                || row + shapeHeight > inventory.height()) {
            return false;
        }

        List<InventoryItem.Cell> occupied = ShapeUtils.getOccupied(col, row, shape, InventoryItem.Orientation.UP);

        for (int i = 0; i < shapeHeight; i++) {
            int[] shapeRow = shape.get(i);
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

    public boolean addItem(String itemId, int col, int row) {
        List<int[]> shape = AllItems.getItem(itemId).getShape();

        int shapeHeight = ShapeUtils.getBoundingHeight(shape, InventoryItem.Orientation.UP);
        int shapeWidth  = ShapeUtils.getBoundingWidth(shape, InventoryItem.Orientation.UP);

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
            int[] shapeRow = shape.get(i);
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
                new InventoryItem(itemId, willOccupy, InventoryItem.Orientation.UP, erre)
        );
        InventoryApi.update(user, inventory.items());
        return true;
    }

    public boolean removeItem(int col, int row) {
        InventoryItem item = inventory.items().stream()
                .filter(i -> i.cells().contains(new InventoryItem.Cell(col, row)))
                .findFirst()
                .orElse(null);
        if (item == null) {
            return false;
        }
        inventory.items().remove(item);
        InventoryApi.update(user, inventory.items());
        return true;
    }

    public boolean rotateItem(int col, int row, boolean clockwise) {
        InventoryItem item = inventory.items().stream()
                .filter(i -> i.cells().contains(new InventoryItem.Cell(col, row)))
                .findFirst()
                .orElse(null);
        if (item == null) {
            return false;
        }

        List<int[]> shape = item.toItem().getShape();
        int shapeHeight = ShapeUtils.getBoundingHeight(shape, item.orientation());
        int shapeWidth  = ShapeUtils.getBoundingWidth(shape, item.orientation());

        if (col < 0 || row < 0
                || col + shapeWidth  > inventory.width()
                || row + shapeHeight > inventory.height()) {
            return false;
        }

        List<InventoryItem.Cell> willOccupy = ShapeUtils.getOccupied(col, row, shape, item.orientation().rotate(clockwise));

        List<InventoryItem> collisionedItems = inventory.items().stream()
                .filter(i -> willOccupy.stream().anyMatch(c -> i.cells().contains(c)))
                .toList();

        if (!collisionedItems.isEmpty()) return false;

        inventory.items().remove(item);
        inventory.items().add(new InventoryItem(item.id(), willOccupy, item.orientation().rotate(clockwise), item.erre()));
        InventoryApi.update(user, inventory.items());
        return true;
    }

    public boolean moveItem(int col, int row, int newCol, int newRow, boolean swap) {
        InventoryItem item = inventory.items().stream()
                .filter(i -> i.cells().contains(new InventoryItem.Cell(col, row)))
                .findFirst()
                .orElse(null);

        if (item == null) return false;

        List<int[]> shape = item.toItem().getShape();
        int shapeHeight = ShapeUtils.getBoundingHeight(shape, item.orientation());
        int shapeWidth  = ShapeUtils.getBoundingWidth(shape, item.orientation());

        if (newCol < 0 || newRow < 0
                || newCol + shapeWidth  > inventory.width()
                || newRow + shapeHeight > inventory.height()) {
            return false;
        }

        List<InventoryItem.Cell> willOccupy = ShapeUtils.getOccupied(newCol, newRow, shape, item.orientation());

        List<InventoryItem> collisionedItems = inventory.items().stream()
                .filter(i -> willOccupy.stream().anyMatch(c -> i.cells().contains(c)))
                .toList();

        if (swap) {

            if (collisionedItems.size() > 1) return false;

            InventoryItem otherItem = collisionedItems.getFirst();
            inventory.items().remove(otherItem);
            inventory.items().add(new InventoryItem(otherItem.id(), willOccupy, item.orientation(), item.erre()));
            inventory.items().remove(item);
            inventory.items().add(new InventoryItem(item.id(), willOccupy, item.orientation(), item.erre()));

            InventoryApi.update(user, inventory.items());
            return true;
        }

        if (!collisionedItems.isEmpty()) return false;

        inventory.items().remove(item);
        inventory.items().add(new InventoryItem(item.id(), willOccupy, item.orientation(), item.erre()));
        InventoryApi.update(user, inventory.items());
        return true;
    }

}
