// CarvingSiteApi.java
package app.caquita.carving;

import app.caquita.auth.AuthUtils;
import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ToolItemKind;
import app.caquita.registry.AllItems;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class CarvingSiteApi {

    private static final ConcurrentHashMap<Long, CarvingSite> userSites = new ConcurrentHashMap<>();

    public static void generate(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        GenerateCarvingSitePayload payload = ctx.bodyAsClass(GenerateCarvingSitePayload.class);

        if (!isValidLocation(payload.lat(), payload.lon())) {
            ctx.status(400).result("Invalid location");
            return;
        }

        boolean ongoing = userSites.containsKey(userId);

        if (ongoing) {
            ctx.status(400).result("You already have an ongoing carving site");
            return;
        }

        CarvingSite site = userSites.computeIfAbsent(userId, k ->
                generateSiteForLocation(payload.lat(), payload.lon()));

        ctx.json(Map.of(
                "site", site,
                "matrix", getTerrainMatrixOnlyCarvables(site)
        ));
    }

    public static void carve(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        CarvingSite site = userSites.get(userId);

        CarvePayload payload = ctx.bodyAsClass(CarvePayload.class);

        if (site == null) {
            ctx.status(400).result("Site not found");
            return;
        }

        ItemKind item = AllItems.getItem(payload.item());

        if (!(item instanceof ToolItemKind tool)) return;

        site.carveGroup(tool, payload.x(), payload.y());

        ctx.json(Map.of(
                "site", site,
                "matrix", getTerrainMatrixOnlyCarvables(site)
        ));
    }

    public static void getSite(Context ctx) {
        long userId = AuthUtils.getUser(ctx);

        CarvingSite site = userSites.get(userId);

        if (site == null) {
            ctx.status(400).result("Site not found");
            return;
        }

        ctx.json(site);
    }

    private static CarvingSite generateSiteForLocation(double lat, double lon) {
        Random paramRandom = new Random(System.currentTimeMillis());

        double noiseScale = 0.2 + paramRandom.nextDouble() * 0.4;

        int minObstacles = 1 + paramRandom.nextInt(3);  // 1-3
        int maxObstacles = minObstacles + paramRandom.nextInt(3); // minObstacles to minObstacles+2

        int minItems = 2 + paramRandom.nextInt(3);  // 2-4
        int maxItems = minItems + paramRandom.nextInt(4); // minItems to minItems+3

        SiteGenerator.GenerationParams params = new SiteGenerator.GenerationParams(
                new SiteGenerator.TerrainParams(noiseScale),
                new SiteGenerator.ObstacleParams(minObstacles, maxObstacles, 50),
                new SiteGenerator.ItemParams(minItems, maxItems, 50)
        );

        long seed = generateSeed(lat, lon) + paramRandom.nextLong();
        return new DirtSite(seed, params);
    }

    private static boolean isValidLocation(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    private static long generateSeed(double lat, double lon) {
        int gridLat = (int) Math.floor(lat * 1000);
        int gridLon = (int) Math.floor(lon * 1000);
        long baseSeed = ((long) gridLat << 32) | (gridLon & 0xffffffffL);
        return baseSeed + System.currentTimeMillis() / 10000; // Adds variation every 10 seconds
    }

    public static int[][] getTerrainMatrixOnlyCarvables(CarvingSite site) {
        List<String> carvables = site.carvables();
        CarvingSite.CarvingCell[][] layout = site.layout();
        int[][] matrix = new int[site.height()][site.width()];

        for (int y = 0; y < site.height(); y++) {
            for (int x = 0; x < site.width(); x++) {
                String carvable = layout[y][x].carvable();
                matrix[y][x] = carvables.indexOf(carvable);
            }
        }

        return matrix;
    }

    record GenerateCarvingSitePayload(double lat, double lon) {}

    record CarvePayload(String item, int x, int y, int[][] shape) {}
}