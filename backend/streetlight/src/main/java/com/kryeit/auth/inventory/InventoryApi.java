package com.kryeit.auth.inventory;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import io.javalin.http.Context;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryApi {

    public static GridInventory getInventory(long user) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM inventories WHERE user_id = :user_id")
                        .bind("user_id", user)
                        .mapTo(GridInventory.class)
                        .findFirst()
                        .orElseGet(() -> {
                            initInventory(user);
                            return getInventory(user);
                        })
        );
    }

    public static void initInventory(long user) {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("INSERT INTO inventories (user_id, width, height, item_positions) VALUES (:user_id, :width, :height, cast(:item_positions as jsonb))")
                    .bind("user_id", user)
                    .bind("width", 4)
                    .bind("height", 4)
                    .bind("item_positions", "{}")
                    .execute();
        });

        GridInventory inventory = getInventory(user);
        InventoryManager manager = new InventoryManager(inventory);

        Item item1 = new Item("sword", "Sword", 1, 2);
        Item item2 = new Item("shield", "Shield", 2, 1);
        Item item3 = new Item("potion", "Potion", 1, 1);

        manager.addItem(item1, new Position(0, 0));
        manager.addItem(item2, new Position(1, 2));
        manager.addItem(item3, new Position(3, 3));

        updateInventory(inventory);
    }

    public static void getInventory(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);

        // Map the item positions to a format the frontend can understand
        List<Map<String, Object>> itemsWithPositions = new ArrayList<>();

        inventory.itemPositions().forEach((item, position) -> {
            Map<String, Object> entry = new HashMap<>();

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("name", item.getName());
            itemMap.put("width", item.getWidth());
            itemMap.put("height", item.getHeight());

            Map<String, Integer> posMap = new HashMap<>();
            posMap.put("x", position.x());
            posMap.put("y", position.y());

            entry.put("item", itemMap);
            entry.put("position", posMap);

            itemsWithPositions.add(entry);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("id", inventory.id());
        response.put("userId", inventory.userId());
        response.put("width", inventory.width());
        response.put("height", inventory.height());
        response.put("itemPositions", itemsWithPositions);

        context.json(response);
    }

    public static void addItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);

        AddItemRequest request = context.bodyAsClass(AddItemRequest.class);
        InventoryManager manager = new InventoryManager(inventory);

        boolean success = manager.addItem(request.item(), request.position());
        if (!success) {
            throw new BadRequestResponse("Cannot add item at the specified position");
        }

        updateInventory(inventory);
        context.status(200).json(Map.of("success", true));
    }

    public static void removeItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);

        RemoveItemRequest request = context.bodyAsClass(RemoveItemRequest.class);
        Item itemToRemove = findItemById(inventory, request.itemId());

        if (itemToRemove == null) {
            throw new NotFoundResponse("Item not found");
        }

        InventoryManager manager = new InventoryManager(inventory);
        boolean success = manager.removeItem(itemToRemove);

        if (!success) {
            throw new BadRequestResponse("Failed to remove item");
        }

        updateInventory(inventory);
        context.status(200).json(Map.of("success", true));
    }

    public static void moveItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);

        MoveItemRequest request = context.bodyAsClass(MoveItemRequest.class);
        Item itemToMove = findItemById(inventory, request.itemId());

        if (itemToMove == null) {
            throw new NotFoundResponse("Item not found");
        }

        InventoryManager manager = new InventoryManager(inventory);
        boolean success = manager.moveItem(itemToMove, request.newPosition());

        if (!success) {
            throw new BadRequestResponse("Cannot move item to the specified position");
        }

        updateInventory(inventory);
        context.status(200).json(Map.of("success", true));
    }

    public static void canPlaceItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);

        CanPlaceItemRequest request = context.bodyAsClass(CanPlaceItemRequest.class);
        Item itemToCheck = null;

        if (request.itemId() != null) {
            itemToCheck = findItemById(inventory, request.itemId());
            if (itemToCheck == null) {
                throw new NotFoundResponse("Item not found");
            }
        } else if (request.item() != null) {
            itemToCheck = request.item();
        } else {
            throw new BadRequestResponse("Either itemId or item must be provided");
        }

        InventoryManager manager = new InventoryManager(inventory);
        boolean canPlace = manager.canPlaceItem(itemToCheck, request.position());

        context.status(200).json(Map.of("canPlace", canPlace));
    }

    private static void updateInventory(GridInventory inventory) {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE inventories SET width = :width, height = :height, item_positions = cast(:item_positions as jsonb) WHERE id = :id AND user_id = :user_id")
                    .bind("id", inventory.id())
                    .bind("user_id", inventory.userId())
                    .bind("width", inventory.width())
                    .bind("height", inventory.height())
                    .bind("item_positions", JsonUtils.serialize(inventory.itemPositions()))
                    .execute();
        });
    }

    private static Item findItemById(GridInventory inventory, String itemId) {
        for (Item item : inventory.itemPositions().keySet()) {
            System.out.println(item.getId());
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    record AddItemRequest(Item item, Position position) {}
    record RemoveItemRequest(String itemId) {}
    record MoveItemRequest(String itemId, Position newPosition) {}
    record CanPlaceItemRequest(String itemId, Item item, Position position) {}
}