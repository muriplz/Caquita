package app.caquita.carving;

import app.caquita.carving.obstacles.CarvingObstacle;
import app.caquita.carving.obstacles.CarvingObstacleInstance;
import app.caquita.content.items.ItemKind;
import app.caquita.registry.AllItems;

import java.util.List;
import java.util.Random;

public class SiteGenerator {
    private static void generateTerrain(CarvingSite.CarvingCell[][] layout, CarvingSite site, TerrainParams params, Random random) {
        List<String> carvables = site.carvables();
        int height = site.height();
        int width = site.width();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int randomIndex = random.nextInt(carvables.size());
                layout[y][x] = new CarvingSite.CarvingCell(carvables.get(randomIndex));
            }
        }

        for (int iteration = 0; iteration < 3; iteration++) {
            applyCellularRules(layout, carvables, width, height);
        }

        ensureTerrainDistribution(layout, carvables, width, height, random);
    }

    private static void applyCellularRules(CarvingSite.CarvingCell[][] layout, List<String> carvables, int width, int height) {
        CarvingSite.CarvingCell[][] newLayout = new CarvingSite.CarvingCell[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String currentType = layout[y][x].carvable();
                int[] neighborCounts = countNeighbors(layout, x, y, width, height, carvables);

                int maxCount = -1;
                String dominantType = currentType;

                for (int i = 0; i < carvables.size(); i++) {
                    if (neighborCounts[i] >= 3 && neighborCounts[i] > maxCount) {
                        maxCount = neighborCounts[i];
                        dominantType = carvables.get(i);
                    }
                }

                newLayout[y][x] = new CarvingSite.CarvingCell(dominantType);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                layout[y][x] = newLayout[y][x];
            }
        }
    }

    private static int[] countNeighbors(CarvingSite.CarvingCell[][] layout, int x, int y, int width, int height, List<String> carvables) {
        int[] counts = new int[carvables.size()];

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    String neighborType = layout[ny][nx].carvable();
                    int index = carvables.indexOf(neighborType);
                    if (index >= 0) counts[index]++;
                }
            }
        }

        return counts;
    }

    private static void ensureTerrainDistribution(CarvingSite.CarvingCell[][] layout, List<String> carvables, int width, int height, Random random) {
        int totalCells = width * height;
        int minPerType = totalCells / (carvables.size() * 2);

        for (String carvable : carvables) {
            int currentCount = countTerrainType(layout, carvable, width, height);

            if (currentCount < minPerType) {
                int needed = minPerType - currentCount;
                for (int i = 0; i < needed; i++) {
                    int x = random.nextInt(width);
                    int y = random.nextInt(height);
                    layout[y][x] = new CarvingSite.CarvingCell(carvable);
                }
            }
        }
    }

    private static int countTerrainType(CarvingSite.CarvingCell[][] layout, String type, int width, int height) {
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout[y][x].carvable().equals(type)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static void generateLayout(CarvingSite.CarvingCell[][] layout, CarvingSite site, GenerationParams params, long seed) {
        Random random = new Random(seed);
        generateTerrain(layout, site, params.terrain(), random);
        placeObstacles(layout, site, params.obstacles(), random);
        placeItems(layout, site, params.items(), random);
    }

    private static void placeObstacles(CarvingSite.CarvingCell[][] layout, CarvingSite site, ObstacleParams params, Random random) {
        List<CarvingObstacle> availableObstacles = site.obstacles();
        int count = params.minCount() + random.nextInt(params.maxCount() - params.minCount() + 1);

        for (int i = 0; i < count; i++) {
            CarvingObstacle obstacle = availableObstacles.get(random.nextInt(availableObstacles.size()));

            for (int attempt = 0; attempt < params.maxAttempts(); attempt++) {
                int x = random.nextInt(site.width());
                int y = random.nextInt(site.height());

                if (canPlaceAt(site, obstacle.shape(), x, y)) {
                    site.placedObstacles().add(new CarvingObstacleInstance(obstacle.id(), x, y));
                    break;
                }
            }
        }
    }

    private static void placeItems(CarvingSite.CarvingCell[][] layout, CarvingSite site, ItemParams params, Random random) {
        List<String> itemIds = site.itemPool();
        ItemKind[] availableItems = itemIds.stream()
                .map(AllItems::getItem)
                .toArray(ItemKind[]::new);

        int count = params.minCount() + random.nextInt(params.maxCount() - params.minCount() + 1);

        for (int i = 0; i < count; i++) {
            ItemKind item = availableItems[random.nextInt(availableItems.length)];

            for (int attempt = 0; attempt < params.maxAttempts(); attempt++) {
                int x = random.nextInt(site.width());
                int y = random.nextInt(site.height());

                if (canPlaceAt(site, item.getShape(), x, y)) {
                    site.placedItems().add(new CarvingItem(item.getId(), 0.5f, x, y));
                    break;
                }
            }
        }
    }

    private static boolean canPlaceAt(CarvingSite site, int[][] shape, int x, int y) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int posX = x + j;
                    int posY = y + i;

                    if (posX < 0 || posX >= site.width() || posY < 0 || posY >= site.height()) {
                        return false;
                    }

                    if (site.getObstacleAt(posX, posY) != null || site.getItemAt(posX, posY) != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public record GenerationParams(
            TerrainParams terrain,
            ObstacleParams obstacles,
            ItemParams items
    ) {}

    public record TerrainParams(
            double noiseScale
    ) {}

    public record ObstacleParams(
            int minCount,
            int maxCount,
            int maxAttempts
    ) {}

    public record ItemParams(
            int minCount,
            int maxCount,
            int maxAttempts
    ) {}
}