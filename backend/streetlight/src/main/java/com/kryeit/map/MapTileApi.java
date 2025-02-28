package com.kryeit.map;

import com.kryeit.Config;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapTileApi {
    private static final Logger logger = LoggerFactory.getLogger(MapTileApi.class);
    private static final String CACHE_DIRECTORY = "map_cache";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    // Stadia Maps URL template with no labels style
    private static final String TILE_URL = "https://tiles.stadiamaps.com/tiles/stamen_watercolor/%d/%d/%d.png?api_key=%s";

    static {
        File cacheDir = new File(CACHE_DIRECTORY);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public static void getTile(Context ctx) {
        int zoom = ctx.pathParamAsClass("zoom", Integer.class).get();
        int x = ctx.pathParamAsClass("x", Integer.class).get();
        int y = ctx.pathParamAsClass("y", Integer.class).get();

        if (zoom < 0 || zoom > 19 || x < 0 || y < 0) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Invalid tile coordinates");
            return;
        }

        try {
            byte[] tileData = getTileFromCache(zoom, x, y);

            if (tileData == null) {
                tileData = fetchTile(zoom, x, y);

                if (tileData != null) {
                    saveTileToCache(zoom, x, y, tileData);
                } else {
                    ctx.status(HttpStatus.NOT_FOUND);
                    return;
                }
            }

            ctx.contentType("image/png");
            ctx.header("Cache-Control", "public, max-age=2592000");
            ctx.result(tileData);

        } catch (IOException e) {
            logger.error("Error serving map tile", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static byte[] getTileFromCache(int zoom, int x, int y) throws IOException {
        File tileFile = getTileCachePath(zoom, x, y).toFile();

        if (tileFile.exists() && tileFile.isFile()) {
            return Files.readAllBytes(tileFile.toPath());
        }

        return null;
    }

    private static byte[] fetchTile(int zoom, int x, int y) throws IOException {
        String urlString = String.format(TILE_URL, zoom, x, y, Config.stadiaKey);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", "Kryeit Map Tile Proxy");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            logger.warn("Failed to fetch tile: {}, status: {}", urlString, responseCode);
            return null;
        }

        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            return out.toByteArray();
        }
    }

    private static void saveTileToCache(int zoom, int x, int y, byte[] data) throws IOException {
        Path tilePath = getTileCachePath(zoom, x, y);
        Files.createDirectories(tilePath.getParent());
        Files.write(tilePath, data);
    }

    private static Path getTileCachePath(int zoom, int x, int y) {
        return Path.of(CACHE_DIRECTORY, String.valueOf(zoom), String.valueOf(x), y + ".png");
    }
}