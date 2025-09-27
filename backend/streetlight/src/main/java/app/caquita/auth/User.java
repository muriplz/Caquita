package app.caquita.auth;

import app.caquita.auth.inventory.InventoryItem;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.json.Json;

import java.sql.Timestamp;
import java.util.List;

public record User(
        long id, String username, String password, Timestamp creation, Timestamp connection,
        Trust trust, String avatar) {

    public enum Trust {
        DEFAULT,
        TRUSTED,
        CONTRIBUTOR,
        MODERATOR,
        ADMINISTRATOR,
        ;
    }

    public record Currencies(long id, long userId, int experience, int beans) {}

    public record Inventory(long id, long userId,
                            @ColumnName("items") @Json List<InventoryItem> items,
                            int width, int height,
                            int tools) {
    }

}