package com.kryeit.auth.inventory;

import com.google.gson.JsonArray;
import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.kryeit.content.items.Item;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class InventoryApi {

    public static Inventory getInventory(long user) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM inventories WHERE user_id = :user_id")
                        .bind("user_id", user)
                        .mapTo(Inventory.class)
                        .findFirst()
                        .orElseGet(() -> {
                            initInventory(user);
                            return getInventory(user);
                        })
        );
    }

    public static void initInventory(long user) {
        InventoryItem item = new InventoryItem(
                "plastic:bottle",
                new int[]{0, 2}
        );

        JsonArray items = new JsonArray();
        items.add(item.toJson());
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("INSERT INTO inventories (user_id, items, height, width) VALUES (:user_id, cast(:item_placements as jsonb), height, width)")
                    .bind("user_id", user)
                    .bind("height", 3)
                    .bind("width", 2)
                    .bind("items", items)
                    .execute();
        });
    }

    public static void getInventory(@NotNull Context context) {
        long user = AuthUtils.getUser(context);

        Inventory inventory = getInventory(user);

        context.json(inventory);
    }

    public static void addItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);

        AddItemRequest request = context.bodyAsClass(AddItemRequest.class);
        InventoryManager manager = new InventoryManager(user);

        boolean success = manager.addItem(request.item, request.slot);
        if (!success) {
            throw new BadRequestResponse("Cannot add item at the specified position");
        }
        context.status(200);
    }

    public static void removeItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        RemoveItemRequest request = context.bodyAsClass(RemoveItemRequest.class);

        InventoryManager manager = new InventoryManager(user);

        if (manager.removeItem(request.item)) {
            context.status(200);
        } else {
            throw new BadRequestResponse("Failed to remove item");
        }
    }

    public static void moveItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        MoveItemRequest request = context.bodyAsClass(MoveItemRequest.class);

        InventoryManager manager = new InventoryManager(user);

        if (manager.moveItem(request.item, request.newSlot)) {
            context.status(200);
        } else {
            throw new BadRequestResponse("Failed to move item");
        }
    }

    public static void canPlaceItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);

        CanPlaceItemRequest request = context.bodyAsClass(CanPlaceItemRequest.class);
        InventoryManager manager = new InventoryManager(user);

        if (manager.calculatePlacement(request.item, request.slot) != null) {
            context.status(200);
        } else {
            throw new BadRequestResponse("Cannot place item at the specified position");
        }
    }

    record AddItemRequest(Item item, int slot) {}
    record RemoveItemRequest(InventoryItem item) {}
    record MoveItemRequest(InventoryItem item, int newSlot) {}
    record CanPlaceItemRequest(Item item, int slot) {}
}