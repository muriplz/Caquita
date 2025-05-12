package com.kryeit.auth.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryeit.Database;

import java.util.List;

public record Inventory(long id, long userId, List<InventoryItem> items, int height, int width) {

    public static void update(long user, List<InventoryItem> items) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String itemsJson = objectMapper.writeValueAsString(items);

            Database.getJdbi().withHandle(handle ->
                handle.createUpdate("""
                        UPDATE SET items = :items
                        WHERE user_id = :userId
                        """)
                        .bind("userId", user)
                        .bind("items", itemsJson)
                        .execute()
            );
        } catch (JsonProcessingException ignored) {}
    }
}