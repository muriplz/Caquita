package com.kryeit.auth.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.content.items.Item;
import com.kryeit.registry.CaquitaItems;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String id;
    private int[] cells;
    private String nbt;

    public InventoryItem() {
        this.id = "";
        this.cells = new int[0];
        this.nbt = "{}";
    }

    public InventoryItem(@JsonProperty("id") String id,
                         @JsonProperty("cells") int[] cells,
                         @JsonProperty("nbt") String nbt) {
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

    public ObjectNode toJson() {
        ObjectNode json = MAPPER.createObjectNode();
        json.put("id", id);
        json.put("nbt", nbt);
        ArrayNode cellsArray = MAPPER.createArrayNode();
        for (int cell : cells) {
            cellsArray.add(cell);
        }
        json.set("cells", cellsArray);
        return json;
    }

    public static List<InventoryItem> fromJsonArray(ArrayNode json) {
        List<InventoryItem> items = new ArrayList<>();
        for (JsonNode element : json) {
            items.add(fromJsonElement(element));
        }
        return items;
    }

    public static InventoryItem fromJsonElement(JsonNode element) {
        String id = element.get("id").asText();
        ArrayNode cellsNode = (ArrayNode) element.get("cells");
        int[] cells = new int[cellsNode.size()];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = cellsNode.get(i).asInt();
        }
        String nbt = element.get("nbt").asText();
        return new InventoryItem(id, cells, nbt);
    }
}