package com.kryeit.auth.inventory;

import com.google.gson.JsonArray;

import java.util.UUID;

public record Inventory(long id, UUID userId, JsonArray items, int height, int width) {
}