package com.kryeit.auth.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.kryeit.content.items.Item;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class InventoryApi {
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        ArrayNode bottlePos = MAPPER.createArrayNode();
        bottlePos.add(MAPPER.createObjectNode().put("col", 0).put("row", 0));
        bottlePos.add(MAPPER.createObjectNode().put("col", 0).put("row", 1));

        ArrayNode pizzaBoxPos = MAPPER.createArrayNode();
        pizzaBoxPos.add(MAPPER.createObjectNode().put("col", 1).put("row", 2));
        pizzaBoxPos.add(MAPPER.createObjectNode().put("col", 1).put("row", 1));
        pizzaBoxPos.add(MAPPER.createObjectNode().put("col", 0).put("row", 2));

        InventoryItem bottle = new InventoryItem(
                "glass:bottle",
                bottlePos
        );

        InventoryItem pizzaBox = new InventoryItem(
                "cardboard:pizza_box",
                pizzaBoxPos
        );

        ArrayNode items = MAPPER.createArrayNode();
        items.add(bottle.toJson());
        items.add(pizzaBox.toJson());

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("INSERT INTO inventories (user_id, items, height, width) VALUES (:user_id, cast(:items as jsonb), :height, :width)")
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

        InventoryItem item = manager.addItem(request.item, request.col, request.row);
        if (item != null) {
            context.status(200).json(item);
        } else {
            throw new BadRequestResponse("Failed to add item");
        }
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

        InventoryItem item = manager.moveItem(request.item, request.newCol, request.newRow);

        if (item != null) {
            context.status(200).json(item);
        } else {
            throw new BadRequestResponse("Failed to move item");
        }
    }

    public static void canPlaceItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);

        CanPlaceItemRequest request = context.bodyAsClass(CanPlaceItemRequest.class);
        System.out.println(request);
        InventoryManager manager = new InventoryManager(user);

        if (manager.calculateMovePlacement(request.item, request.col, request.row) != null) {
            context.status(200);
        } else {
            throw new BadRequestResponse("Cannot place item at the specified position");
        }
    }

    record AddItemRequest(Item item, int col, int row) {}
    record RemoveItemRequest(InventoryItem item) {}
    record MoveItemRequest(InventoryItem item, int newCol, int newRow) {}
    record CanPlaceItemRequest(InventoryItem item, int col, int row) {}
}