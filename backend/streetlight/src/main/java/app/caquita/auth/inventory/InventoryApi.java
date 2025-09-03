package app.caquita.auth.inventory;

import app.caquita.auth.AuthUtils;
import app.caquita.auth.User;
import app.caquita.storage.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.http.Context;

import java.util.List;

public class InventoryApi {

    public static void update(long user, List<InventoryItem> items) {
        try {
            String itemsJson = Database.MAPPER.writeValueAsString(items);

            Database.getJdbi().withHandle(handle ->
                    handle.createUpdate("""
                        UPDATE inventories SET items = cast(:items AS jsonb)
                        WHERE user_id = :userId
                        """)
                            .bind("userId", user)
                            .bind("items", itemsJson)
                            .execute()
            );
        } catch (JsonProcessingException ignored) {}
    }

    public static User.Inventory getInventory(long user) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM inventories WHERE user_id = :user_id")
                        .bind("user_id", user)
                        .mapTo(User.Inventory.class)
                        .findFirst()
                        .orElseGet(() -> {
                            initInventory(user);
                            return getInventory(user);
                        })
        );
    }

    public static void initInventory(long user) {
        List<InventoryItem.Cell> bottlePos = List.of(
                new InventoryItem.Cell(2, 1),
                new InventoryItem.Cell(2, 2)
        );

        List<InventoryItem.Cell> pipePos = List.of(
                new InventoryItem.Cell(0, 0),
                new InventoryItem.Cell(1, 0),
                new InventoryItem.Cell(2, 0),
                new InventoryItem.Cell(0, 1)
        );

        InventoryItem bottle = new InventoryItem(
                "plastic:bottle",
                bottlePos,
                0.5f
        );

        InventoryItem pipe = new InventoryItem(
                "plastic:pipe",
                pipePos,
                0.5f
        );

        List<InventoryItem> items = List.of(bottle, pipe);
        try {
            String itemsJson = Database.MAPPER.writeValueAsString(items);

            Database.getJdbi().useHandle(handle ->
                    handle.createUpdate("""
                        INSERT INTO inventories (user_id, items, height, width)
                        VALUES (:user_id, cast(:items AS jsonb), :height, :width)
                        """)
                            .bind("user_id", user)
                            .bind("items",   itemsJson)
                            .bind("height",  4)
                            .bind("width",   3)
                            .execute()
            );
        } catch (JsonProcessingException ignored) {}
    }

    public static void getInventory(Context ctx) {
        long user = AuthUtils.getUser(ctx);

        User.Inventory inventory = getInventory(user);

        ctx.status(200).json(inventory);
    }

    public static void addItem(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        AddItemPayload payload = ctx.bodyAsClass(AddItemPayload.class);

        InventoryManager manager = new InventoryManager(user);

        if (manager.addItem(payload.itemId(), payload.anchor.col(), payload.anchor.row())) {
            ctx.status(200).json(manager.inventory);
        } else {
            ctx.status(400).result("Failed to add item");
        }
    }

    public static void removeItem(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        RemoveItemPayload payload = ctx.bodyAsClass(RemoveItemPayload.class);

        InventoryManager manager = new InventoryManager(user);

        if (manager.removeItem(payload.anchor.col(), payload.anchor.row())) {
            ctx.status(200).json(manager.inventory);
        } else {
            ctx.status(400).result("Failed to remove item");
        }
    }

    public static void moveItem(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        MoveItemPayload payload = ctx.bodyAsClass(MoveItemPayload.class);

        InventoryManager manager = new InventoryManager(user);

        if (manager.moveItem(payload.anchor.col(), payload.anchor.row(), payload.newAnchor.col(), payload.newAnchor.row())) {
            ctx.status(200).json(manager.inventory);
        } else {
            ctx.status(400).result("Failed to move item");
        }
    }

    public static void canPlaceItem(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        CanPlaceItemRequest payload = ctx.bodyAsClass(CanPlaceItemRequest.class);

        InventoryManager manager = new InventoryManager(user);

        if (manager.canPlaceItem(payload.itemId(), payload.anchor.col(), payload.anchor.row())) {
            ctx.status(200).result("true");
        } else {
            ctx.status(200).result("false");
        }
    }

    record AddItemPayload(String itemId, InventoryItem.Cell anchor) {}
    record RemoveItemPayload(InventoryItem.Cell anchor) {}
    record MoveItemPayload(InventoryItem.Cell anchor, InventoryItem.Cell newAnchor) {}
    record CanPlaceItemRequest(String itemId, InventoryItem.Cell anchor) {}
}