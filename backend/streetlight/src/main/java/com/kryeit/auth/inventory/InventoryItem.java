package com.kryeit.auth.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kryeit.content.items.Item;
import com.kryeit.registry.CaquitaItems;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem {
    private String id;
    private int[] cells;
    private String nbt;

    public InventoryItem(String id, int[] cells, String nbt) {
        this.id = id;
        this.cells = cells;
        this.nbt = nbt;
    }

    public InventoryItem(String id, int[] cells) {
        this.id = id;
        this.cells = cells;
        this.nbt = "{}";
    }

    public String id() {
        return id;
    }

    public int[] cells() {
        return cells;
    }

    public String nbt() {
        return nbt;
    }

    public void setCells(int[] cells) {
        this.cells = cells;
    }

    public void setNbt(String nbt) {
        this.nbt = nbt;
    }

    public Item toItem() {
        return CaquitaItems.getItem(id);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("nbt", nbt);
        JsonArray cellsArray = new JsonArray();
        for (int cell : cells) {
            cellsArray.add(cell);
        }
        json.add("cells", cellsArray);
        return json;
    }

    public static List<InventoryItem> fromJsonArray(JsonArray json) {
        List<InventoryItem> items = new ArrayList<>();
        for (JsonElement element : json) {
            items.add(fromJsonElement(element));
        }
        return items;
    }

    public static InventoryItem fromJsonElement(JsonElement element) {
        JsonObject item = element.getAsJsonObject();
        String id = item.get("id").getAsString();
        int[] cells = new int[item.getAsJsonArray("cells").size()];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = item.getAsJsonArray("cells").get(i).getAsInt();
        }
        String nbt = item.get("nbt").getAsString();
        return new InventoryItem(id, cells, nbt);
    }
}
