package app.caquita.auth.inventory.clothes.dusters;

import app.caquita.auth.AuthUtils;
import app.caquita.auth.inventory.clothes.Wardrobe;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.http.Context;

public class DusterApi {

    public static void addTool(Context ctx) {
        long userId = AuthUtils(ctx);

        AddToolPayload payload = ctx.bodyAsClass(AddTollPayload.class);

        // Take the tool, if none then ctx 400 and return
        // Add tool with Wardrobe.addTool

        
    }
    record AddToolPayload(int col, int row) {}

    public static void equipTool(Context ctx) throws JsonProcessingException {
        long userId = AuthUtils.getUser(ctx);
        EquipToolPayload payload = ctx.bodyAsClass(EquipToolPayload.class);

        Wardrobe wardrobe = Wardrobe.equipOrSwapTool(userId, payload.col(), payload.row());

        ctx.status(200).json(wardrobe);
    }
    record EquipToolPayload(int col, int row) {}

    public static void swapTools(Context ctx) throws JsonProcessingException {
        long userId = AuthUtils.getUser(ctx);
        SwapToolsPayload payload = ctx.bodyAsClass(SwapToolsPayload.class);

        Wardrobe wardrobe = Wardrobe.swapPocketTools(userId, payload.col1(), payload.row1(), payload.col2(), payload.row2());

        ctx.status(200).json(wardrobe);
    }
    record SwapToolsPayload(int col1, int row1, int col2, int row2) {}
}
