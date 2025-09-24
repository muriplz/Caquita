package app.caquita.auth.inventory.tools;

import app.caquita.auth.AuthUtils;
import app.caquita.auth.inventory.InventoryApi;
import app.caquita.auth.inventory.InventoryManager;
import app.caquita.content.items.ToolItemKind;
import app.caquita.registry.AllItems;
import io.javalin.http.Context;

public class ToolInventoryApi {

    public static void get(Context ctx) {
        long userId = AuthUtils.getUser(ctx);

        ctx.status(200).json(Tool.getToolInventory(userId));
    }

    public static void addTool(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        AddToolPayload payload = ctx.bodyAsClass(AddToolPayload.class);

        if (!(AllItems.getItem(payload.toolId()) instanceof ToolItemKind tool)) {
            return;
        }

        boolean hasItem = InventoryApi.hasItem(userId, payload.toolId());

        if (!hasItem) {
            ctx.status(400);
            return;
        }

        if (!new InventoryManager(userId).removeItem(payload.toolId(), payload.erre)) {
            ctx.status(400);
            return;
        }

        if (Tool.addTool(userId, tool, payload.erre)) {
            ctx.status(200);
        }

    }
    record AddToolPayload(String toolId, float erre) {}

}
