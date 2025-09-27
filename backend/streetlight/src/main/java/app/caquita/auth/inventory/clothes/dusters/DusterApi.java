package app.caquita.auth.inventory.clothes.dusters;

import app.caquita.auth.AuthUtils;
import app.caquita.auth.inventory.clothes.Wardrobe;
import app.caquita.content.items.ToolItemKind;
import app.caquita.registry.AllItems;
import app.caquita.storage.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.http.Context;
import org.json.JSONObject;

public class DusterApi {

    public static void dropTool(Context ctx) throws JsonProcessingException {
        long userId = AuthUtils.getUser(ctx);

        DropToolPayload payload = ctx.bodyAsClass(DropToolPayload.class);

        Wardrobe wardrobe = deleteTool(userId, payload.pocketCol, payload.pocketRow);
        // TODO: remove exp

        ctx.status(200).json(wardrobe);
    }
    record DropToolPayload(int pocketCol, int pocketRow) {}

    public static void addTool(Context ctx) throws JsonProcessingException {
        long userId = AuthUtils.getUser(ctx);
        AddToolPayload payload = ctx.bodyAsClass(AddToolPayload.class);

        Wardrobe wardrobe = Database.getJdbi().withHandle(handle -> {
            return handle.inTransaction(h -> {
                Wardrobe w = Wardrobe.get(userId);
                EquippedDuster duster = w.getDuster();

                // Find empty pocket first
                EquippedDuster.ToolPocket[][] shape = duster.toolPocketsShape();
                int targetCol = -1, targetRow = -1;
                for (int col = 0; col < shape.length && targetCol == -1; col++) {
                    for (int row = 0; row < shape[col].length; row++) {
                        if (shape[col][row].tool() == null && !shape[col][row].locked()) {
                            targetCol = col;
                            targetRow = row;
                            break;
                        }
                    }
                }
                if (targetCol == -1) return w; // No space

                // Get and delete item from inventory
                EquippedDuster.ToolData item = h.createQuery("SELECT items ->> 'id' AS toolId, 0 AS durability, (items ->> 'erre')::float AS erre FROM inventories, jsonb_array_elements(items) AS items WHERE user_id = :user_id AND (items ->> 'col')::int = :col AND (items ->> 'row')::int = :row")
                        .bind("user_id", userId)
                        .bind("col", payload.inventoryCol)
                        .bind("row", payload.inventoryRow)
                        .mapTo(EquippedDuster.ToolData.class)
                        .findFirst()
                        .orElse(null);

                if (item == null) return w; // No item found

                h.createUpdate("UPDATE inventories SET items = (SELECT jsonb_agg(i) FROM jsonb_array_elements(items) AS i WHERE (i ->> 'col')::int != :col OR (i ->> 'row')::int != :row) WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .bind("col", payload.inventoryCol)
                        .bind("row", payload.inventoryRow)
                        .execute();

                // Create tool with proper durability
                ToolItemKind toolKind = (ToolItemKind) AllItems.getItem(item.toolId());
                EquippedDuster.ToolData newTool = new EquippedDuster.ToolData(item.toolId(), toolKind.getMaxDurability(), item.erre());

                // Add to duster
                EquippedDuster.ToolPocket[][] newShape = shape.clone();
                newShape[targetCol][targetRow] = new EquippedDuster.ToolPocket(false, newTool);

                EquippedDuster newDuster = new EquippedDuster(duster.duster(), duster.equippedTool(), duster.pockets(), newShape);
                String newDusterJson = Database.MAPPER.writeValueAsString(newDuster);

                h.createUpdate("UPDATE wardrobes SET duster = cast(? as jsonb) WHERE user_id = ?")
                        .bind(0, newDusterJson)
                        .bind(1, userId)
                        .execute();

                return new Wardrobe(w.id(), w.userId(), new JSONObject(newDusterJson), w.dusters());
            });
        });

        ctx.status(200).json(wardrobe);
    }
    record AddToolPayload(int inventoryCol, int inventoryRow) {}

    public static void equipTool(Context ctx) throws JsonProcessingException {
        long userId = AuthUtils.getUser(ctx);
        EquipToolPayload payload = ctx.bodyAsClass(EquipToolPayload.class);

        Wardrobe wardrobe = equipOrSwapTool(userId, payload.col(), payload.row());

        ctx.status(200).json(wardrobe);
    }
    record EquipToolPayload(int col, int row) {}

    public static void swapTools(Context ctx) throws JsonProcessingException {
        long userId = AuthUtils.getUser(ctx);
        SwapToolsPayload payload = ctx.bodyAsClass(SwapToolsPayload.class);

        Wardrobe wardrobe = swapPocketTools(userId, payload.col1(), payload.row1(), payload.col2(), payload.row2());

        ctx.status(200).json(wardrobe);
    }
    record SwapToolsPayload(int col1, int row1, int col2, int row2) {}


    // WARDROBE DB METHODS

    public static Wardrobe swapPocketTools(long userId, int col, int row, int newCol, int newRow) throws JsonProcessingException {
        Wardrobe wardrobe = Wardrobe.get(userId);
        EquippedDuster duster = wardrobe.getDuster();

        EquippedDuster.ToolPocket[][] shape = duster.toolPocketsShape().clone();
        EquippedDuster.ToolData toolA = shape[col][row].tool();
        EquippedDuster.ToolData toolB = shape[newCol][newRow].tool();

        shape[col][row] = new EquippedDuster.ToolPocket(shape[col][row].locked(), toolB);
        shape[newCol][newRow] = new EquippedDuster.ToolPocket(shape[newCol][newRow].locked(), toolA);

        EquippedDuster newDuster = new EquippedDuster(
                duster.duster(),
                duster.equippedTool(),
                duster.pockets(),
                shape
        );

        String newDusterJson = Database.MAPPER.writeValueAsString(newDuster);
        Database.getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE wardrobes SET duster = cast(? as jsonb) WHERE user_id = ?")
                        .bind(0, newDusterJson)
                        .bind(1, userId)
                        .execute()
        );

        return new Wardrobe(wardrobe.id(), wardrobe.userId(), new JSONObject(newDusterJson), wardrobe.dusters());
    }

    public static Wardrobe equipOrSwapTool(long userId, int col, int row) throws JsonProcessingException {
        Wardrobe wardrobe = Wardrobe.get(userId);
        EquippedDuster duster = wardrobe.getDuster();
        EquippedDuster.ToolData equippedTool = duster.equippedTool();
        EquippedDuster.ToolData pocketTool = duster.toolPocketsShape()[col][row].tool();

        EquippedDuster.ToolPocket[][] newShape = duster.toolPocketsShape().clone();
        newShape[col][row] = new EquippedDuster.ToolPocket(newShape[col][row].locked(), equippedTool);

        EquippedDuster newDuster = new EquippedDuster(
                duster.duster(),
                pocketTool,
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

        return new Wardrobe(wardrobe.id(), wardrobe.userId(), new JSONObject(newDusterJson), wardrobe.dusters());
    }

    public static Wardrobe deleteTool(long userId, int pocketCol, int pocketRow) throws JsonProcessingException {
        Wardrobe wardrobe = Wardrobe.get(userId);
        EquippedDuster duster = wardrobe.getDuster();

        EquippedDuster.ToolPocket[][] newShape = duster.toolPocketsShape().clone();
        newShape[pocketCol][pocketRow] = new EquippedDuster.ToolPocket(newShape[pocketCol][pocketRow].locked(), null);

        EquippedDuster newDuster = new EquippedDuster(
                duster.duster(),
                duster.equippedTool(),
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

        return new Wardrobe(wardrobe.id(), wardrobe.userId(), new JSONObject(newDusterJson), wardrobe.dusters());
    }
}