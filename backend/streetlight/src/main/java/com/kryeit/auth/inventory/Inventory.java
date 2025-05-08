package com.kryeit.auth.inventory;

import com.fasterxml.jackson.databind.node.ArrayNode;

public record Inventory(long id, long userId, ArrayNode items, int height, int width) {
}