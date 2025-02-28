package com.kryeit.map;

import com.kryeit.Database;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MapTileDatabase {
    private static final Logger logger = LoggerFactory.getLogger(MapTileDatabase.class);

    static {
        // Create tables if they don't exist
        Database.getJdbi().useHandle(handle -> {
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS map_tiles (" +
                            "id SERIAL PRIMARY KEY, " +
                            "zoom INTEGER NOT NULL, " +
                            "x INTEGER NOT NULL, " +
                            "y INTEGER NOT NULL, " +
                            "provider VARCHAR(50) NOT NULL, " +
                            "tile_data BYTEA NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "CONSTRAINT unique_tile UNIQUE(zoom, x, y, provider)" +
                            ")"
            );

            handle.execute(
                    "CREATE INDEX IF NOT EXISTS idx_tile_coords ON map_tiles(zoom, x, y)"
            );
        });
    }

    // Get a tile from the database
    public static byte[] getTile(int zoom, int x, int y, String provider) {
        try {
            byte[] tileData = Database.getJdbi().withHandle(handle ->
                    handle.createQuery("SELECT tile_data FROM map_tiles WHERE zoom = :zoom AND x = :x AND y = :y AND provider = :provider")
                            .bind("zoom", zoom)
                            .bind("x", x)
                            .bind("y", y)
                            .bind("provider", provider)
                            .mapTo(byte[].class)
                            .findOne()
                            .orElse(null)
            );

            if (tileData != null) {
                // Update last accessed time
                updateLastAccessed(zoom, x, y, provider);
            }

            return tileData;
        } catch (Exception e) {
            logger.error("Error retrieving tile from database", e);
            return null;
        }
    }

    // Save a tile to the database
    public static void saveTile(int zoom, int x, int y, String provider, byte[] tileData) {
        try {
            Database.getJdbi().useHandle(handle -> {
                handle.createUpdate(
                                "INSERT INTO map_tiles (zoom, x, y, provider, tile_data) " +
                                        "VALUES (:zoom, :x, :y, :provider, :tileData) " +
                                        "ON CONFLICT (zoom, x, y, provider) DO UPDATE " +
                                        "SET tile_data = EXCLUDED.tile_data, last_accessed = CURRENT_TIMESTAMP"
                        )
                        .bind("zoom", zoom)
                        .bind("x", x)
                        .bind("y", y)
                        .bind("provider", provider)
                        .bind("tileData", tileData)
                        .execute();
            });
        } catch (Exception e) {
            logger.error("Error saving tile to database", e);
        }
    }

    // Update the last accessed time for a tile
    private static void updateLastAccessed(int zoom, int x, int y, String provider) {
        try {
            Database.getJdbi().useHandle(handle -> {
                handle.createUpdate(
                                "UPDATE map_tiles SET last_accessed = CURRENT_TIMESTAMP " +
                                        "WHERE zoom = :zoom AND x = :x AND y = :y AND provider = :provider"
                        )
                        .bind("zoom", zoom)
                        .bind("x", x)
                        .bind("y", y)
                        .bind("provider", provider)
                        .execute();
            });
        } catch (Exception e) {
            logger.error("Error updating last accessed time", e);
        }
    }

    // Clean up old tiles
    public static int deleteOldTiles(int daysToKeep) {
        try {
            return Database.getJdbi().withHandle(handle ->
                    handle.createUpdate(
                                    "DELETE FROM map_tiles WHERE last_accessed < (CURRENT_TIMESTAMP - INTERVAL ':days days'::interval)"
                            )
                            .bind("days", daysToKeep)
                            .execute()
            );
        } catch (Exception e) {
            logger.error("Error deleting old tiles", e);
            return 0;
        }
    }

    // Get tile statistics
    public static Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Total tiles
            Integer totalTiles = Database.getJdbi().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(*) FROM map_tiles")
                            .mapTo(Integer.class)
                            .one()
            );
            stats.put("totalTiles", totalTiles);

            // Tiles by zoom level
            Map<Integer, Integer> zoomStats = Database.getJdbi().withHandle(handle -> {
                Map<Integer, Integer> result = new HashMap<>();
                handle.createQuery("SELECT zoom, COUNT(*) as count FROM map_tiles GROUP BY zoom ORDER BY zoom")
                        .mapToMap()
                        .forEach(row -> result.put(
                                (Integer) row.get("zoom"),
                                ((Number) row.get("count")).intValue())
                        );
                return result;
            });
            stats.put("zoomLevels", zoomStats);

            // Storage size
            String dbSize = Database.getJdbi().withHandle(handle ->
                    handle.createQuery("SELECT pg_size_pretty(pg_total_relation_size('map_tiles')) as size")
                            .mapTo(String.class)
                            .one()
            );
            stats.put("databaseSize", dbSize);

            // Oldest and newest tiles
            Database.getJdbi().useHandle(handle -> {
                handle.registerRowMapper(new DateStatsMapper());
                Map<String, Object> dateStats = handle.createQuery(
                                "SELECT min(created_at) as oldest, max(created_at) as newest FROM map_tiles"
                        )
                        .map(new DateStatsMapper())
                        .one();

                stats.putAll(dateStats);
            });

        } catch (Exception e) {
            logger.error("Error getting tile statistics", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // Custom mapper for date statistics
    private static class DateStatsMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> map(ResultSet rs, StatementContext ctx) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            result.put("oldestTile", rs.getTimestamp("oldest"));
            result.put("newestTile", rs.getTimestamp("newest"));
            return result;
        }
    }
}