package com.kryeit.map;

import com.kryeit.Config;
import io.javalin.http.Context;

import java.net.HttpURLConnection;
import java.net.URL;

public class MapApi {

    public static void getTile(Context ctx) throws Exception {
        String lat = ctx.pathParam("x");
        String lon = ctx.pathParam("y");

        if (!lat.matches("\\d+") || !lon.matches("\\d+")) {
            ctx.status(400).result("Invalid tile coordinates");
            return;
        }

        String url = String.format(
                "https://api.maptiler.com/maps/0196969b-fe27-7361-9ac7-f4678b1621d1/static/%s,%s,16/512x512@2x.png?key=%s",
                lat,
                lon,
                Config.MAP_TILER_KEY
        );

        HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        if (code != 200) return;

        ctx.contentType("image/png");
        ctx.result(conn.getInputStream());
    }
}
