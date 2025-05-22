package app.caquita.landmark.trash_can;

import com.fasterxml.jackson.core.JsonProcessingException;
import app.caquita.Database;
import app.caquita.auth.inventory.InventoryItem;
import app.caquita.landmark.Landmark;
import app.caquita.landmark.LandmarkType;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.json.Json;

import java.util.List;

public record TrashCan(
        long id, double lat, double lon,
        String name, String description, long author,
        boolean broken,
        boolean ashtray, boolean windblown, boolean flooded, boolean overwhelmed, boolean poopbag, boolean art
) implements Landmark {

    @Override
    public LandmarkType type() {
        return LandmarkType.TRASH_CAN;
    }

    public record Inventory(long id, long trashCanId,
                            @ColumnName("items") @Json List<InventoryItem> items,
                            int height, int width) {

        public static void update(long trashCan, List<InventoryItem> items) {
            try {
                String itemsJson = Database.MAPPER.writeValueAsString(items);

                Database.getJdbi().withHandle(handle ->
                        handle.createUpdate("""
                                        UPDATE trash_can_inventories SET items = cast(:items AS jsonb)
                                        WHERE trash_can_id = :trashCanId
                                        """)
                                .bind("trashCanId", trashCan)
                                .bind("items", itemsJson)
                                .execute()
                );
            } catch (JsonProcessingException ignored) {
            }
        }
    }
}
