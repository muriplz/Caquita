package app.caquita.auth.inventory.tools;

import app.caquita.content.items.ToolItemKind;
import app.caquita.storage.Database;

import java.util.List;

public record Tool(
        long id, long userId,
        String item,
        int durability
) {

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

    public static List<Tool> getInventory(long userId) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM tools WHERE user_id = :userId")
                        .bind("userId", userId)
                        .mapTo(Tool.class)
                        .list()
        );
    }

    public static boolean addTool(long userId, ToolItemKind tool) {
        return Database.getJdbi().withHandle(handle -> {
            int currentCount = getToolCount(userId);
            int maxCount = getMaxToolCount(userId);

            if (currentCount >= maxCount) {
                return false;
            }

            handle.createUpdate("""
                INSERT INTO tools (user_id, item, durability) VALUES (:userId, :item, :durability)
                """)
                    .bind("userId", userId)
                    .bind("item", tool.getId())
                    .bind("durability", tool.getMaxDurability())
                    .execute();

            return true;
        });
    }

    public static void removeTool(long userId, String toolId) {
        Database.getJdbi().withHandle(handle ->
                handle.createUpdate("""
                        DELETE FROM tools WHERE user_id = :userId AND item = :toolId
                        """)
                        .bind("userId", userId)
                        .bind("toolId", toolId)
                        .execute()
        );
    }

    public static boolean carve(long userId, String toolId) {
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
