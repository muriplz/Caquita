package app.caquita.carving;

import app.caquita.auth.AuthUtils;
import app.caquita.carving.obstacles.CarvingObstacleInstance;
import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ToolItemKind;
import app.caquita.registry.AllItems;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;
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
            userSites.remove(userId);
            //ctx.status(400).result("You already have an ongoing carving site");
            //return;
        }

        CarvingSite site = userSites.computeIfAbsent(userId, k ->
                generateSiteForLocation(payload.lat(), payload.lon()));

        ctx.json(getEnhancedTerrainMatrix(site));
    }

    public static void carve(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        CarvingSite site = userSites.get(userId);

        CarvePayload payload = ctx.bodyAsClass(CarvePayload.class);

        if (site == null) {
            ctx.status(400).result("Site not found");
            return;
        }

        ItemKind item = AllItems.getItem(payload.tool());

        if (!(item instanceof ToolItemKind tool)) return;

        site.carveGroup(tool, payload.x(), payload.y());

        ctx.json(getEnhancedTerrainMatrix(site));
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

    public static TerrainMatrix getEnhancedTerrainMatrix(CarvingSite site) {
        CarvingSite.CarvingCell[][] layout = site.layout();
        int height = site.height();
        int width = site.width();

        String[][] terrain = new String[height][width];
        List<CarvingObstacleInstance> visibleObstacles = new ArrayList<>();
        List<CarvingItem> visibleItems = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                terrain[y][x] = layout[y][x].carvable();
            }
        }

        // Only include obstacles with at least one empty cell
        for (CarvingObstacleInstance obstacle : site.placedObstacles()) {
            if (hasAtLeastOneEmptyCell(site, obstacle)) {
                visibleObstacles.add(obstacle);
            }
        }

        // Only include items with at least one empty cell
        for (CarvingItem item : site.placedItems()) {
            if (hasAtLeastOneEmptyCell(site, item)) {
                visibleItems.add(item);
            }
        }

        return new TerrainMatrix(terrain, visibleObstacles, visibleItems);
    }

    private static boolean hasAtLeastOneEmptyCell(CarvingSite site, CarvingObstacleInstance obstacle) {
        int[][] shape = obstacle.shape();
        for (int sy = 0; sy < shape.length; sy++) {
            for (int sx = 0; sx < shape[sy].length; sx++) {
                if (shape[sy][sx] == 1) {
                    int x = obstacle.anchorX() + sx;
                    int y = obstacle.anchorY() + sy;
                    if (x >= 0 && x < site.width() && y >= 0 && y < site.height()) {
                        if (site.layout()[y][x].isFullyCarved()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasAtLeastOneEmptyCell(CarvingSite site, CarvingItem item) {
        int[][] shape = item.shape();
        for (int sy = 0; sy < shape.length; sy++) {
            for (int sx = 0; sx < shape[sy].length; sx++) {
                if (shape[sy][sx] == 1) {
                    int x = item.anchorX() + sx;
                    int y = item.anchorY() + sy;
                    if (x >= 0 && x < site.width() && y >= 0 && y < site.height()) {
                        if (site.layout()[y][x].isFullyCarved()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public record TerrainMatrix(String[][] carvables, List<CarvingObstacleInstance> obstacles, List<CarvingItem> items) {}

    record GenerateCarvingSitePayload(double lat, double lon) {}

    record CarvePayload(String tool, int x, int y) {}
}