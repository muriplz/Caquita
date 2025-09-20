package app.caquita.carving.obstacles;

public class Sprinkler implements CarvingObstacle {
    @Override
    public String id() {
        return "obstacle:sprinkler";
    }

    @Override
    public int[][] shape() {
        return new int[][] {
                {1, 1},
                {1, 1}
        };
    }
}
