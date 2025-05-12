package com.kryeit.content.items;

import com.kryeit.registry.CaquitaItems;
import io.javalin.http.Context;

public class ItemKindsApi {
    public static void getItem(Context ctx) {
        String id = ctx.pathParam("id");
        ItemKind item = CaquitaItems.getItem(id);

        if (item == null) {
            ctx.status(404).result("Item not found");
        } else {
            ctx.status(200).json(item);
        }
    }
}
