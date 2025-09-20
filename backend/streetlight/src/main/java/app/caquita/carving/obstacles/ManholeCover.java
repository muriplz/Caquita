package app.caquita.carving.obstacles;

public class ManholeCover implements CarvingObstacle {
    @Override
    public String id() {
        return "obstacle:manhole_cover";
    }

    @Override
    public int[][] shape() {
        return new int[][] {
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };
    }
}
