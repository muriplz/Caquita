package app.caquita.carving.obstacles.dirt;

import app.caquita.carving.CarvingSite;
import app.caquita.carving.obstacles.CarvingObstacle;

public class ManholeCover implements CarvingObstacle {
    @Override
    public String id() {
        return "dirt:manhole_cover";
    }

    @Override
    public int[][] shape() {
        return new int[][] {
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };
    }

    @Override
    public CarvingSite.SiteType type() {
        return CarvingSite.SiteType.DIRT;
    }
}
