package com.kryeit.auth.inventory;

import com.google.gson.*;
import com.kryeit.content.items.Item;
import com.kryeit.registry.CaquitaItems;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(GridInventory.class, new GridInventoryAdapter())
            .create();

    public static String serialize(GridInventory inventory) {
        return gson.toJson(inventory);
    }

    public static GridInventory deserialize(String json, long id, long userId) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        int width = jsonObject.has("width") ? jsonObject.get("width").getAsInt() : 4;
        int height = jsonObject.has("height") ? jsonObject.get("height").getAsInt() : 4;

        GridInventory inventory = new GridInventory(id, userId, width, height);

        if (jsonObject.has("itemPlacements")) {
            JsonArray itemsArray = jsonObject.getAsJsonArray("itemPlacements");

            for (JsonElement element : itemsArray) {
                JsonObject itemObj = element.getAsJsonObject();

                String instanceId = itemObj.get("instanceId").getAsString();
                String itemId = itemObj.get("itemId").getAsString();

                JsonObject posObj = itemObj.getAsJsonObject("position");
                int x = posObj.get("x").getAsInt();
                int y = posObj.get("y").getAsInt();

                Item item = CaquitaItems.getItem(itemId);
                if (item != null) {
                    InventoryItem inventoryItem = new InventoryItem(instanceId, item);
                    Position position = new Position(x, y);

                    inventory.itemPlacements().put(instanceId,
                            new GridInventory.ItemPlacement(inventoryItem, position));
                }
            }
        }

        return inventory;
    }

    private static class GridInventoryAdapter implements JsonSerializer<GridInventory>, JsonDeserializer<GridInventory> {
        @Override
        public JsonElement serialize(GridInventory src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("id", src.id());
            result.addProperty("userId", src.userId());
            result.addProperty("width", src.width());
            result.addProperty("height", src.height());

            JsonArray itemsArray = new JsonArray();

            for (Map.Entry<String, GridInventory.ItemPlacement> entry : src.itemPlacements().entrySet()) {
                String instanceId = entry.getKey();
                GridInventory.ItemPlacement placement = entry.getValue();
                InventoryItem item = placement.item();
                Position position = placement.position();

                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("instanceId", instanceId);
                itemObj.addProperty("itemId", item.getItemId());

                JsonObject posObj = new JsonObject();
                posObj.addProperty("x", position.x());
                posObj.addProperty("y", position.y());

                itemObj.add("position", posObj);
                itemsArray.add(itemObj);
            }

            result.add("itemPlacements", itemsArray);
            return result;
        }

        @Override
        public GridInventory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            long id = jsonObject.get("id").getAsLong();
            long userId = jsonObject.get("userId").getAsLong();
            int width = jsonObject.get("width").getAsInt();
            int height = jsonObject.get("height").getAsInt();

            GridInventory inventory = new GridInventory(id, userId, width, height);

            if (jsonObject.has("itemPlacements")) {
                JsonArray itemsArray = jsonObject.getAsJsonArray("itemPlacements");

                for (JsonElement element : itemsArray) {
                    JsonObject itemObj = element.getAsJsonObject();

                    String instanceId = itemObj.get("instanceId").getAsString();
                    String itemId = itemObj.get("itemId").getAsString();

                    JsonObject posObj = itemObj.getAsJsonObject("position");
                    int x = posObj.get("x").getAsInt();
                    int y = posObj.get("y").getAsInt();

                    Item item = CaquitaItems.getItem(itemId);
                    if (item != null) {
                        InventoryItem inventoryItem = new InventoryItem(instanceId, item);
                        Position position = new Position(x, y);

                        inventory.itemPlacements().put(instanceId,
                                new GridInventory.ItemPlacement(inventoryItem, position));
                    }
                }
            }

            return inventory;
        }
    }
}