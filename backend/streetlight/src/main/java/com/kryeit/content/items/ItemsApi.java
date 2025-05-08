package com.kryeit.content.items;

import com.kryeit.registry.CaquitaItems;
import io.javalin.http.Context;

public class ItemsApi {
    public static void getItem(Context ctx) {
        String id = ctx.pathParam("id");
        Item item = CaquitaItems.getItem(id);

        if (item == null) {
            ctx.status(404).result("Item not found");
        } else {
            ctx.json(item);
        }
    }
}
