package com.kryeit.auth.inventory;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public record Cell(int col, int row) {

    public static Cell of(JsonNode node) {
        return new Cell(node.get("col").asInt(), node.get("row").asInt());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell that = (Cell) o;
        return col == that.col && row == that.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }
}
