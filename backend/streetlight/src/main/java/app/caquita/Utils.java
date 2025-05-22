package app.caquita;

import io.javalin.http.Context;

public class Utils {

    public static long getIdFromParam(Context ctx) {
        try {
            return Long.parseLong(ctx.queryParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid id format");
            return -1;
        }
    }

    public static long getIdFromPath(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid id format");
            return -1;
        }
    }
}
