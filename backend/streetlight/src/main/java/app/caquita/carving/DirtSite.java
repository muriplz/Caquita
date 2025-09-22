package app.caquita.carving;

import app.caquita.carving.obstacles.*;

import java.util.ArrayList;
import java.util.List;

public class DirtSite implements CarvingSite {

    private final CarvingCell[][] layout;
    private final List<CarvingObstacleInstance> placedObstacles = new ArrayList<>();
    private final List<CarvingItem> placedItems = new ArrayList<>();

    public DirtSite(long seed, SiteGenerator.GenerationParams params) {
        this.layout = new CarvingCell[height()][width()];

        SiteGenerator.generateLayout(layout, this, params, seed);
    }

    @Override
    public List<String> carvables() {
        return List.of(
                "dirt:gravel",
                "dirt:coarse_dirt",    // index 3
                "dirt:dirt",           // index 2
                "dirt:grass",          // index 1

                "dirt:grown_grass"    // index 0 (surface)
                        // index 4 (deepest)
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

    @Override
    public List<CarvingObstacleInstance> placedObstacles() {
        return placedObstacles;
    }

    @Override
    public List<CarvingItem> placedItems() {
        return placedItems;
    }
}