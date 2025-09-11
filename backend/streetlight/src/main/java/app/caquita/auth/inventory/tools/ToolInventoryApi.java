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

        ctx.status(200).json(Tool.getInventory(userId));
    }

    public static void addTool(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        AddToolPayload payload = ctx.bodyAsClass(AddToolPayload.class);

        if (!(AllItems.getItem(payload.toolId()) instanceof ToolItemKind tool)) {
            return;
        }
        
        boolean hasItem = InventoryApi.hasItem(userId, payload.toolId());

        if (!hasItem) {
            ctx.status(400).json("You don't have this item in your inventory");
            return;
        }
        boolean added = Tool.addTool(userId, tool);

        if (added) {
            ctx.status(200).json(new InventoryManager(userId).removeItem(payload.toolId()));
        }

    }
    record AddToolPayload(String toolId) {}

    public static void removeTool(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        RemoveToolPayload payload = ctx.bodyAsClass(RemoveToolPayload.class);

        Tool.removeTool(userId, payload.toolId());
        ctx.status(200);
    }
    record RemoveToolPayload(String toolId) {}

    public static void decreaseDurability(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        DecreaseDurabilityPayload payload = ctx.bodyAsClass(DecreaseDurabilityPayload.class);

        boolean broken = Tool.decreaseDurability(userId, payload.toolId(), payload.amount());
        ctx.status(200).json(broken);
    }
    record DecreaseDurabilityPayload(String toolId, int amount) {}
}
