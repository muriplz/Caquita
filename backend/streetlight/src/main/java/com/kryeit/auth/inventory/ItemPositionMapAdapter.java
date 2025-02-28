package com.kryeit.auth.inventory;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ItemPositionMapAdapter implements JsonSerializer<Map<Item, Position>>, JsonDeserializer<Map<Item, Position>> {

    @Override
    public JsonElement serialize(Map<Item, Position> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();

        for (Map.Entry<Item, Position> entry : src.entrySet()) {
            JsonObject object = new JsonObject();

            // Serialize item
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("id", entry.getKey().getId());
            itemObj.addProperty("name", entry.getKey().getName());
            itemObj.addProperty("width", entry.getKey().getWidth());
            itemObj.addProperty("height", entry.getKey().getHeight());

            // Serialize position
            JsonObject posObj = new JsonObject();
            posObj.addProperty("x", entry.getValue().x());
            posObj.addProperty("y", entry.getValue().y());

            // Add to object
            object.add("item", itemObj);
            object.add("position", posObj);

            // Add to array
            array.add(object);
        }

        return array;
    }

    @Override
    public Map<Item, Position> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Map<Item, Position> map = new HashMap<>();

        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();

            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();

                    // Get item
                    JsonObject itemObj = object.getAsJsonObject("item");
                    String id = itemObj.get("id").getAsString();
                    String name = itemObj.get("name").getAsString();
                    int width = itemObj.get("width").getAsInt();
                    int height = itemObj.get("height").getAsInt();

                    Item item = new Item(id, name, width, height);

                    // Get position
                    JsonObject posObj = object.getAsJsonObject("position");
                    int x = posObj.get("x").getAsInt();
                    int y = posObj.get("y").getAsInt();

                    Position position = new Position(x, y);

                    // Add to map
                    map.put(item, position);
                }
            }
        }

        return map;
    }
}