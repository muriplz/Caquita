package app.caquita.carving;

import app.caquita.carving.obstacles.CarvingObstacle;
import app.caquita.carving.obstacles.ManholeCover;
import app.caquita.carving.obstacles.MetalPole;
import app.caquita.carving.obstacles.Sprinkler;

import java.util.List;

public class DirtSite implements CarvingSite {

    private final CarvingCell[][] layout;

    public DirtSite(long seed, SiteGenerator.GenerationParams params) {
        this.layout = new CarvingCell[height()][width()];

        SiteGenerator.generateLayout(layout, this, params, seed);
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