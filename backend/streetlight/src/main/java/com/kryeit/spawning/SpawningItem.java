package com.kryeit.spawning;

import com.kryeit.Database;
import com.kryeit.sync.LocationSyncable;

import java.sql.Timestamp;
import java.util.List;

public record SpawningItem(
        long id, long itemId, long userId, double lat,
        double lon, Timestamp creation, Timestamp duration
) {

    public static void delete(long user, double lat, double lon) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("DELETE FROM spawning_items WHERE user_id = ? AND lat = ? AND lon = ?")
                        .bind(0, user)
                        .bind(1, lat)
                        .bind(2, lon)
                        .execute()
        );
    }

    public static List<SpawningItem> get(long user, double lat, double lon) {
        int radius = SpawningItemGenerator.RADIUS_AROUND_PLAYER;

        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM spawning_items WHERE user_id = ? AND lat BETWEEN ? AND ? AND lon BETWEEN ? AND ?")
                        .bind(0, user)
                        .bind(1, lat - radius)
                        .bind(2, lat + radius)
                        .bind(3, lon - radius)
                        .bind(4, lon + radius)
                        .mapTo(SpawningItem.class)
                        .list()
        );
    }

}