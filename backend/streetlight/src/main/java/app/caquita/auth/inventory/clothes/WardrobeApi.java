package app.caquita.auth.inventory.clothes;

import app.caquita.auth.AuthUtils;
import io.javalin.http.Context;

public class WardrobeApi {

    public static void get(Context ctx) {
        long userId = AuthUtils.getUser(ctx);

        ctx.status(200).json(Wardrobe.get(userId));
    }
}
