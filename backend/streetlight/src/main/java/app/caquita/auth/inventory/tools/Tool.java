package app.caquita.auth.inventory.tools;

import app.caquita.content.items.ToolItemKind;
import app.caquita.storage.Database;

import java.util.List;

public record Tool(
        long id, long userId,
        String item,
        float erre,
        int durability,
        boolean equipped
) {

    public static void ensureEquippedTool(long userId) {
        Database.getJdbi().useHandle(handle -> {
            int equippedCount = handle.createQuery("SELECT COUNT(*) FROM tools WHERE user_id = :userId AND equipped = TRUE")
                    .bind("userId", userId)
                    .mapTo(int.class)
                    .one();

            if (equippedCount == 0) {
                handle.createUpdate("UPDATE tools SET equipped = TRUE WHERE user_id = :userId ORDER BY id LIMIT 1")
                        .bind("userId", userId)
                        .execute();
            } else if (equippedCount > 1) {
                handle.createUpdate("UPDATE tools SET equipped = FALSE WHERE user_id = :userId AND id NOT IN (SELECT id FROM tools WHERE user_id = :userId AND equipped = TRUE ORDER BY id LIMIT 1)")
                        .bind("userId", userId)
                        .execute();
            }
        });
    }

    public static boolean equip(long userId, long toolId, int durability, float erre) {
        return Database.getJdbi().withHandle(handle -> {
            int updated = handle.createUpdate("""
                UPDATE tools SET equipped = TRUE
                WHERE id = :toolId AND user_id = :userId
                AND durability >= :durability AND erre >= :erre
                """)
                    .bind("toolId", toolId)
                    .bind("userId", userId)
                    .bind("durability", durability)
                    .bind("erre", erre)
                    .execute();

            if (updated > 0) {
                handle.createUpdate("""
                    UPDATE tools SET equipped = FALSE
                    WHERE user_id = :userId AND (id != :toolId OR erre != :erre OR durability != :durability)
                    """)
                        .bind("userId", userId)
                        .bind("toolId", toolId)
                        .execute();
            }

            return updated > 0;
        });

    }

    public static int getToolCount(long userId) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM tools WHERE user_id = :userId")
                        .bind("userId", userId)
                        .mapTo(int.class)
                        .one()
        );
    }

    public static int getMaxToolCount(long userId) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT tools FROM inventories WHERE user_id = :userId")
                        .bind("userId", userId)
                        .mapTo(int.class)
                        .one()
        );
    }

    public static List<Tool> getToolInventory(long userId) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM tools WHERE user_id = :userId")
                        .bind("userId", userId)
                        .mapTo(Tool.class)
                        .list()
        );
    }

    public static boolean addTool(long userId, ToolItemKind tool, float erre) {
        return Database.getJdbi().withHandle(handle -> {
            int currentCount = getToolCount(userId);
            int maxCount = getMaxToolCount(userId);

            if (currentCount >= maxCount) {
                return false;
            }

            handle.createUpdate("""
                INSERT INTO tools (user_id, item, erre, durability) VALUES (:userId, :item, :erre, :durability)
                """)
                    .bind("userId", userId)
                    .bind("item", tool.getId())
                    .bind("erre", erre)
                    .bind("durability", tool.getMaxDurability())
                    .execute();

            return true;
        });
    }

    public static boolean gotBrokenAndCarve(long userId, String toolId) {
        int amount = 1;

        return Database.getJdbi().withHandle(handle -> {
            int deleted = handle.createUpdate("""
            DELETE FROM tools
            WHERE item = :toolId AND durability <= :amount
            AND user_id = :userId
            """)
                    .bind("toolId", toolId)
                    .bind("amount", amount)
                    .bind("userId", userId)
                    .execute();

            if (deleted == 0) {
                handle.createUpdate("""
                UPDATE tools SET durability = durability - :amount
                WHERE item = :toolId
                AND user_id = :userId
                """)
                        .bind("toolId", toolId)
                        .bind("amount", amount)
                        .bind("userId", userId)
                        .execute();
            }

            return deleted > 0;
        });
    }
}
