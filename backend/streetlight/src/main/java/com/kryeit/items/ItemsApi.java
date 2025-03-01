package com.kryeit.items;

import com.kryeit.Utils;
import io.javalin.http.Context;

public class ItemsApi {
    public static void getItems(Context context) {
        context.json(ItemConfigReader.getAllItems());
    }


    public static void getItem(Context context) {
        String id = context.pathParam("id");
        ConfigItem item = ItemConfigReader.getItemById(id);
        if (item == null) {
            context.status(404).result("Item not found");
        } else {
            context.json(item);
        }
    }
}
