package com.kryeit.auth.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonUtils {
    private static final Type ITEM_POSITION_MAP_TYPE = new TypeToken<Map<Item, Position>>(){}.getType();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ITEM_POSITION_MAP_TYPE, new ItemPositionMapAdapter())
            .create();

    public static String serialize(Map<Item, Position> itemPositions) {
        return gson.toJson(itemPositions, ITEM_POSITION_MAP_TYPE);
    }

    public static Map<Item, Position> deserializeItemPositions(String json) {
        return gson.fromJson(json, ITEM_POSITION_MAP_TYPE);
    }
}