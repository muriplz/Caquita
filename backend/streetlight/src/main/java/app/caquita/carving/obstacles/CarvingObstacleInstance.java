package app.caquita.carving.obstacles;

import app.caquita.registry.AllObstacles;

public record CarvingObstacleInstance(
    String obstacle,
    int anchorX,
    int anchorY
) {
    public int[][] shape() {
        return AllObstacles.getObstacle(obstacle).shape();
    }

    public boolean coversCell(int x, int y) {
        int[][] shape = shape();
        int relX = x - anchorX;
        int relY = y - anchorY;

        return relX >= 0 && relX < shape[0].length &&
                relY >= 0 && relY < shape.length &&
                shape[relY][relX] == 1;
    }
}