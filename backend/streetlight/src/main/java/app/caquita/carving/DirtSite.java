// DirtSite.java
package app.caquita.carving;

import app.caquita.carving.obstacles.CarvingObstacle;
import app.caquita.carving.obstacles.dirt.ManholeCover;
import app.caquita.carving.obstacles.dirt.MetalPole;
import app.caquita.carving.obstacles.dirt.Sprinkler;
import app.caquita.content.items.ItemKind;
import app.caquita.registry.AllItems;

import java.util.List;
import java.util.Random;

public class DirtSite implements CarvingSite {

    private final CarvingCell[][] layout;

    public DirtSite(long seed) {
        this.layout = new CarvingCell[height()][width()];
        Random random = new Random(seed);

        generateTerrain(random);
        placeObstacles(random);
        placeItems(random);
    }

    private void generateTerrain(Random random) {
        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                double noise = generateNoise(x, y, 0.3, random);

                if (noise > 0.7) {
                    layout[y][x] = new CarvingCell("dirt:grown_grass");
                } else if (noise > 0.4) {
                    layout[y][x] = new CarvingCell("dirt:grass");
                } else if (noise > 0.1) {
                    layout[y][x] = new CarvingCell("dirt:dirt");
                } else if (noise > -0.2) {
                    layout[y][x] = new CarvingCell("dirt:coarse_dirt");
                } else {
                    layout[y][x] = new CarvingCell("dirt:gravel");
                }
            }
        }
    }

    private void placeObstacles(Random random) {
        CarvingObstacle[] obstacles = {new ManholeCover(), new Sprinkler(), new MetalPole()};
        int count = 2 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            CarvingObstacle obstacle = obstacles[random.nextInt(obstacles.length)];

            for (int attempt = 0; attempt < 50; attempt++) {
                int x = random.nextInt(width());
                int y = random.nextInt(height());

                if (canPlaceAt(obstacle.shape(), x, y)) {
                    placeObstacleAt(obstacle, x, y);
                    break;
                }
            }
        }
    }

    private void placeItems(Random random) {
        ItemKind[] availableItems = AllItems.getAllItems().toArray(new ItemKind[0]);
        int count = 3 + random.nextInt(5);

        for (int i = 0; i < count; i++) {
            ItemKind item = availableItems[random.nextInt(availableItems.length)];

            for (int attempt = 0; attempt < 50; attempt++) {
                int x = random.nextInt(width());
                int y = random.nextInt(height());

                if (canPlaceAt(item.getShape(), x, y)) {
                    placeItemAt(item, x, y);
                    break;
                }
            }
        }
    }

    private boolean canPlaceAt(int[][] shape, int x, int y) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int posX = x + j;
                    int posY = y + i;

                    if (posX < 0 || posX >= width() || posY < 0 || posY >= height()) {
                        return false;
                    }

                    CarvingCell cell = layout[posY][posX];
                    if (cell.hasObstacle() || cell.hasBuriedItem()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placeObstacleAt(CarvingObstacle obstacle, int x, int y) {
        int[][] shape = obstacle.shape();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int posX = x + j;
                    int posY = y + i;

                    CarvingCell current = layout[posY][posX];
                    layout[posY][posX] = new CarvingCell(current.carvable(), obstacle.id(), current.buriedItem());
                }
            }
        }
    }

    private void placeItemAt(ItemKind item, int x, int y) {
        int[][] shape = item.getShape();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int posX = x + j;
                    int posY = y + i;

                    CarvingCell current = layout[posY][posX];
                    layout[posY][posX] = new CarvingCell(current.carvable(), current.obstacle(), item.getId());
                }
            }
        }
    }

    private double generateNoise(int x, int y, double scale, Random random) {
        return Math.sin(x * scale) * Math.cos(y * scale) +
                Math.sin(x * scale * 2) * Math.cos(y * scale * 2) * 0.5 +
                random.nextGaussian() * 0.2;
    }

    @Override
    public List<String> carvables() {
        return List.of(
                "dirt:grown_grass",
                "dirt:grass",
                "dirt:dirt",
                "dirt:coarse_dirt",
                "dirt:gravel"
        );
    }

    @Override
    public List<CarvingObstacle> obstacles() {
        return List.of(
                new ManholeCover(),
                new Sprinkler(),
                new MetalPole()
        );
    }

    @Override
    public List<String> itemPool() {
        return List.of(
                "plastic:bottle",
                "plastic:pipe"
        );
    }

    @Override
    public SiteType type() {
        return SiteType.DIRT;
    }

    @Override
    public int width() {
        return 6;
    }

    @Override
    public int height() {
        return 10;
    }

    @Override
    public CarvingCell[][] layout() {
        return layout;
    }
}