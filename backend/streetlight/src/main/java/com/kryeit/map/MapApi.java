package com.kryeit.map;

import com.kryeit.Config;
import io.javalin.http.Context;

import java.net.HttpURLConnection;
import java.net.URL;

public class MapApi {

    public static void getTile(Context ctx) throws Exception {
        String x = ctx.pathParam("x");
        String y = ctx.pathParam("y");

        if (!x.matches("\\d+") || !y.matches("\\d+")) {
            ctx.status(400).result("Invalid tile coordinates");
            return;
        }

        //String url = String.format(
        //        "%s/styles/caquita/16/%s/%s.png",
        //        Config.TILE_SERVER, x, y
        //);

        String url = "https://tiles.stadiamaps.com/static_cacheable/stamen_watercolor/16/%s/%s.jpg?api_key=0a341668-8943-4d8f-afbf-0088707a9c07";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != 200) return;

        ctx.contentType("image/png");
        ctx.result(connection.getInputStream());
    }
}
