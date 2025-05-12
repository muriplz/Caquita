package com.kryeit.map;

import com.kryeit.Config;
import io.javalin.http.Context;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class MapApi {

    private static final Pattern COORD = Pattern.compile("^-?\\d{1,3}\\.\\d{6}$");
    private static final int ZOOM = 16;

    private static final String CACHE_DIR = "uploads/tiles";

    public static void getTile(Context ctx) throws Exception {
        int x = Integer.parseInt(ctx.pathParam("x"));
        int y = Integer.parseInt(ctx.pathParam("y"));

        File tileFile = new File(CACHE_DIR + File.separator + ZOOM + File.separator + x,
                y + ".jpg");

        tileFile.getParentFile().mkdirs();

        if (tileFile.exists()) {
            ctx.contentType("image/jpeg");
            ctx.status(200).result(new FileInputStream(tileFile));
            return;
        }

        String url = String.format(
                "https://tiles-eu.stadiamaps.com/tiles/stamen_watercolor/%d/%d/%d.jpg?api_key=%s",
                ZOOM, x, y, Config.MAP_TILER_KEY
        );

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int code = conn.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            ctx.status(code).result("Upstream tile server returned " + code);
            return;
        }

        try (InputStream in = conn.getInputStream();
             OutputStream out = new FileOutputStream(tileFile)) {
            in.transferTo(out);
        }

        ctx.contentType("image/jpeg");
        ctx.status(200) .result(new FileInputStream(tileFile));
    }
}
