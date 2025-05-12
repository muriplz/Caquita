package com.kryeit.auth.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kryeit.Database;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.json.Json;

import java.util.List;

public record Inventory(long id, long userId,
                        @ColumnName("items") @Json List<InventoryItem> items,
                        int height, int width) {

    public static void update(long user, List<InventoryItem> items) {
        try {
            String itemsJson = Database.MAPPER.writeValueAsString(items);

            Database.getJdbi().withHandle(handle ->
                    handle.createUpdate("""
                        UPDATE SET items = cast(:items AS jsonb)
                        WHERE user_id = :userId
                        """)
                            .bind("userId", user)
                            .bind("items", itemsJson)
                            .execute()
            );
        } catch (JsonProcessingException ignored) {}
    }
}