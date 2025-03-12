package com.kryeit.auth.inventory;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.kryeit.content.items.Item;
import com.kryeit.registry.CaquitaItems;
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
        GridInventory inventory = new GridInventory(0, user, 4, 4);

        Item plasticTupper = CaquitaItems.getItem("plastic:tupper");

        InventoryManager manager = new InventoryManager(inventory);

        manager.addItem(plasticTupper, new Position(2, 2));

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("INSERT INTO inventories (user_id, width, height, item_placements) VALUES (:user_id, :width, :height, cast(:item_placements as jsonb))")
                    .bind("user_id", user)
                    .bind("width", inventory.width())
                    .bind("height", inventory.height())
                    .bind("item_placements", JsonUtils.serialize(inventory))
                    .execute();
        });
    }

    public static void getInventory(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", inventory.id());
        response.put("userId", inventory.userId());
        response.put("width", inventory.width());
        response.put("height", inventory.height());

        List<Map<String, Object>> itemsList = new ArrayList<>();

        for (Map.Entry<String, GridInventory.ItemPlacement> entry : inventory.itemPlacements().entrySet()) {
            String instanceId = entry.getKey();
            GridInventory.ItemPlacement placement = entry.getValue();
            InventoryItem inventoryItem = placement.item();
            Position position = placement.position();

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("instanceId", instanceId);

            Map<String, Object> itemDetails = new HashMap<>();
            itemDetails.put("id", inventoryItem.getItemId());
            itemDetails.put("width", inventoryItem.getWidth());
            itemDetails.put("height", inventoryItem.getHeight());
            itemDetails.put("rarity", inventoryItem.getRarity().name());

            Map<String, Integer> posMap = new HashMap<>();
            posMap.put("x", position.x());
            posMap.put("y", position.y());

            itemMap.put("item", itemDetails);
            itemMap.put("position", posMap);

            itemsList.add(itemMap);
        }

        response.put("items", itemsList);
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

        InventoryManager manager = new InventoryManager(inventory);

        if (request.instanceId() != null) {
            boolean success = manager.removeItem(request.instanceId());
            if (!success) {
                throw new NotFoundResponse("Item not found with the given instance ID");
            }
        } else if (request.itemId() != null && request.position() != null) {
            InventoryItem itemToRemove = manager.findItemByIdAndPosition(request.itemId(), request.position());
            if (itemToRemove == null) {
                throw new NotFoundResponse("Item not found at the specified position");
            }
            boolean success = manager.removeItem(itemToRemove.getInstanceId());
            if (!success) {
                throw new BadRequestResponse("Failed to remove item");
            }
        } else {
            throw new BadRequestResponse("Either instanceId or (itemId and position) must be provided");
        }

        updateInventory(inventory);
        context.status(200).json(Map.of("success", true));
    }

    public static void moveItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);
        MoveItemRequest request = context.bodyAsClass(MoveItemRequest.class);

        InventoryManager manager = new InventoryManager(inventory);

        if (request.instanceId() != null) {
            boolean success = manager.moveItem(request.instanceId(), request.newPosition());
            if (!success) {
                throw new BadRequestResponse("Cannot move item to the specified position");
            }
        } else if (request.itemId() != null && request.currentPosition() != null) {
            InventoryItem itemToMove = manager.findItemByIdAndPosition(request.itemId(), request.currentPosition());
            if (itemToMove == null) {
                throw new NotFoundResponse("Item not found at the specified position");
            }
            boolean success = manager.moveItem(itemToMove.getInstanceId(), request.newPosition());
            if (!success) {
                throw new BadRequestResponse("Cannot move item to the specified position");
            }
        } else {
            throw new BadRequestResponse("Either instanceId or (itemId and currentPosition) must be provided");
        }

        updateInventory(inventory);
        context.status(200).json(Map.of("success", true));
    }

    public static void canPlaceItem(@NotNull Context context) {
        long user = AuthUtils.getUser(context);
        GridInventory inventory = getInventory(user);

        CanPlaceItemRequest request = context.bodyAsClass(CanPlaceItemRequest.class);
        InventoryManager manager = new InventoryManager(inventory);

        if (request.instanceId() != null) {
            InventoryItem inventoryItem = manager.findItemByInstanceId(request.instanceId());
            if (inventoryItem == null) {
                throw new NotFoundResponse("Item not found with the given instance ID");
            }
            boolean canPlace = manager.canPlaceItem(inventoryItem, request.position(), request.instanceId());
            context.status(200).json(Map.of("canPlace", canPlace));
        } else if (request.itemId() != null) {
            Item item = CaquitaItems.getItem(request.itemId());
            if (item == null) {
                throw new NotFoundResponse("Item not found with the given ID");
            }
            InventoryItem tempItem = new InventoryItem(item);
            boolean canPlace = manager.canPlaceItem(tempItem, request.position());
            context.status(200).json(Map.of("canPlace", canPlace));
        } else if (request.item() != null) {
            InventoryItem tempItem = new InventoryItem(request.item());
            boolean canPlace = manager.canPlaceItem(tempItem, request.position());
            context.status(200).json(Map.of("canPlace", canPlace));
        } else {
            throw new BadRequestResponse("Either instanceId, itemId, or item must be provided");
        }
    }

    private static void updateInventory(GridInventory inventory) {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE inventories SET width = :width, height = :height, item_placements = cast(:item_placements as jsonb) WHERE id = :id AND user_id = :user_id")
                    .bind("id", inventory.id())
                    .bind("user_id", inventory.userId())
                    .bind("width", inventory.width())
                    .bind("height", inventory.height())
                    .bind("item_placements", JsonUtils.serialize(inventory))
                    .execute();
        });
    }

    record AddItemRequest(Item item, Position position) {}
    record RemoveItemRequest(String instanceId, String itemId, Position position) {}
    record MoveItemRequest(String instanceId, String itemId, Position currentPosition, Position newPosition) {}
    record CanPlaceItemRequest(String instanceId, String itemId, Item item, Position position) {}
}