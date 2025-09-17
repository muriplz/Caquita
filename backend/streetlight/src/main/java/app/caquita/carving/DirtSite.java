package app.caquita.carving;

import java.util.List;

public class DirtSite implements CarvingSite {

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
    public List<String> obstacles() {
        return List.of(
                "dirt:manhole_cover",
                "dirt:sprinkler",
                "dirt:metal_post"
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
    public CarvingCell[][] getLayout() {
        return new CarvingCell[][]{};
    }
}
