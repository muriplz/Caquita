package app.caquita.carving.obstacles.dirt;

import app.caquita.carving.CarvingSite;
import app.caquita.carving.obstacles.CarvingObstacle;

public class Sprinkler implements CarvingObstacle {
    @Override
    public String id() {
        return "dirt:sprinkler";
    }

    @Override
    public int[][] shape() {
        return new int[][] {
                {1, 1},
                {1, 1}
        };
    }

    @Override
    public CarvingSite.SiteType type() {
        return CarvingSite.SiteType.DIRT;
    }
}
