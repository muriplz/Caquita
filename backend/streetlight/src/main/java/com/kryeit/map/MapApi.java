package com.kryeit.map;

import com.kryeit.Config;
import io.javalin.http.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class MapApi {

    private static final Pattern COORD = Pattern.compile("^-?\\d{1,3}\\.\\d{6}$");
    private static final int ZOOM = 16;
    // base cache directory (relative to working dir)
    private static final String CACHE_DIR = "tile-cache";

    public static void getTile(Context ctx) throws Exception {
        int x = Integer.parseInt(ctx.pathParam("x"));
        int y = Integer.parseInt(ctx.pathParam("y"));

        // build cache-file path: tile-cache/16/x/y.jpg
        File tileFile = new File(CACHE_DIR + File.separator + ZOOM + File.separator + x,
                y + ".jpg");

        // ensure parent dirs exist
        tileFile.getParentFile().mkdirs();

        if (tileFile.exists()) {
            // serve from cache
            ctx.contentType("image/jpeg");
            ctx.result(new FileInputStream(tileFile));
            return;
        }

        // not cached → fetch upstream
        String url = String.format(
                "https://tiles-eu.stadiamaps.com/tiles/stamen_watercolor/%d/%d/%d.jpg?api_key=%s",
                ZOOM, x, y, Config.MAP_TILER_KEY
        );
        System.out.println("Fetching tile from: " + url);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int code = conn.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            ctx.status(code).result("Upstream tile server returned " + code);
            return;
        }

        // Read upstream bytes and write to cache file
        try (InputStream in = conn.getInputStream();
             OutputStream out = new FileOutputStream(tileFile)) {
            in.transferTo(out);
        }

        // Serve the newly cached file
        ctx.contentType("image/jpeg");
        ctx.result(new FileInputStream(tileFile));
    }
}
