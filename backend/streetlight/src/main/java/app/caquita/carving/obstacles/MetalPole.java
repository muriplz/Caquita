package app.caquita.carving.obstacles;

public class MetalPole implements CarvingObstacle {
    @Override
    public String id() {
        return "obstacle:metal_pole";
    }

    @Override
    public int[][] shape() {
        return new int[][] {
                {1}
        };
    }
}
