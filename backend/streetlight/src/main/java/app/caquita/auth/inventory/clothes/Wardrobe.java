package app.caquita.auth.inventory.clothes;

import app.caquita.auth.inventory.clothes.dusters.EquippedDuster;
import app.caquita.storage.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;

import java.util.List;

public record Wardrobe(
        long id, long userId,
        JSONObject duster,
        List<String> dusters
) {
    public static void init(long userId) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO wardrobes (user_id, duster, dusters)
                        VALUES (:userId, '{}', '[]'::jsonb)
                        """)
                        .bind("userId", userId)
                        .execute()
        );
    }

    public static Wardrobe get(long userId) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM wardrobes WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .mapTo(Wardrobe.class)
                        .findFirst()
                        .orElseGet(() -> {
                            init(userId);
                            return get(userId);
                        })
        );
    }

    public static Wardrobe swapPocketTools(long userId, int col, int row, int newCol, int newRow) throws JsonProcessingException {
        Wardrobe wardrobe = get(userId);
        EquippedDuster duster = wardrobe.getDuster();

        EquippedDuster.ToolPocket[][] shape = duster.toolPocketsShape().clone();
        EquippedDuster.ToolData toolA = shape[col][row].tool();
        EquippedDuster.ToolData toolB = shape[newCol][newRow].tool();

        shape[col][row] = new EquippedDuster.ToolPocket(col, row, toolB);
        shape[newCol][newRow] = new EquippedDuster.ToolPocket(newCol, newRow, toolA);

        EquippedDuster newDuster = new EquippedDuster(
                duster.duster(),
                duster.equippedTool(),
                duster.pockets(),
                shape
        );

        String newDusterJson = Database.MAPPER.writeValueAsString(newDuster);
        // Update database
        Database.getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE wardrobes SET duster = cast(? as jsonb) WHERE user_id = ?")
                        .bind(0, newDusterJson)
                        .bind(1, userId)
                        .execute()
        );

        return new Wardrobe(wardrobe.id, wardrobe.userId, new JSONObject(newDusterJson), wardrobe.dusters);
    }

    public static Wardrobe equipOrSwapTool(long userId, int col, int row) throws JsonProcessingException {
        Wardrobe wardrobe = get(userId);
        EquippedDuster duster = wardrobe.getDuster();
        EquippedDuster.ToolData equippedTool = duster.equippedTool();
        EquippedDuster.ToolData pocketTool = duster.toolPocketsShape()[col][row].tool();

        EquippedDuster.ToolPocket[][] newShape = duster.toolPocketsShape().clone();
        newShape[col][row] = new EquippedDuster.ToolPocket(col, row, equippedTool);

        EquippedDuster newDuster = new EquippedDuster(
                duster.duster(),
                pocketTool, // pocket tool becomes equipped
                duster.pockets(),
                newShape
        );

        String newDusterJson = Database.MAPPER.writeValueAsString(newDuster);

        Database.getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE wardrobes SET duster = cast(? as jsonb) WHERE user_id = ?")
                        .bind(0, newDusterJson)
                        .bind(1, userId)
                        .execute()
        );

        return new Wardrobe(wardrobe.id, wardrobe.userId, new JSONObject(newDusterJson), wardrobe.dusters);
    }

    public EquippedDuster getDuster() throws JsonProcessingException {
        return Database.fromJsonObject(duster, EquippedDuster.class);
    }

}
