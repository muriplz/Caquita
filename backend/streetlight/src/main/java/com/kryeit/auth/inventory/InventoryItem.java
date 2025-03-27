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

public record InventoryItem(@JsonProperty("id") String id,
                            @JsonProperty("cells") ArrayNode cells,
                            @JsonProperty("orientation") Orientation orientation,
                            @JsonProperty("nbt") String nbt) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public InventoryItem() {
        this("", MAPPER.createArrayNode(), Orientation.UP, "{}");
    }

    public InventoryItem(String id, ArrayNode cells) {
        this(id, cells, Orientation.UP, "{}");
    }

    public Item toItem() {
        return CaquitaItems.getItem(id);
    }

    public ObjectNode toJson() {
        ObjectNode json = MAPPER.createObjectNode();
        json.put("id", id);
        json.put("nbt", nbt);
        json.put("orientation", orientation != null ? orientation.name() : "UP");
        ArrayNode cellsArray = MAPPER.createArrayNode();
        for (JsonNode cell : cells) {
            cellsArray.add(cell);
        }
        json.set("cells", cellsArray);
        return json;
    }

    public int getAnchorCol() {
        List<int[]> shape = toItem().getShape();

        ArrayNode cellsArray = cells();

        int minCol = Integer.MAX_VALUE;
        for (JsonNode cell : cellsArray) {
            minCol = Math.min(minCol, cell.get("col").asInt());
        }

        int shapeOffsetX = -1;
        outerLoop:
        for (int x = 0; x < shape.get(0).length; x++) {
            for (int[] ints : shape) {
                if (ints[x] == 1) {
                    shapeOffsetX = x;
                    break outerLoop;
                }
            }
        }

        return minCol - shapeOffsetX;
    }

    public int getAnchorRow() {
        List<int[]> shape = toItem().getShape();

        ArrayNode cellsArray = cells();

        int minRow = Integer.MAX_VALUE;
        for (JsonNode cell : cellsArray) {
            minRow = Math.min(minRow, cell.get("row").asInt());
        }

        int shapeOffsetY = -1;
        outerLoop:
        for (int y = 0; y < shape.size(); y++) {
            for (int x = 0; x < shape.get(y).length; x++) {
                if (shape.get(y)[x] == 1) {
                    shapeOffsetY = y;
                    break outerLoop;
                }
            }
        }

        return minRow - shapeOffsetY;
    }
}