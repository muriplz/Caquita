package app.caquita.carving;

import app.caquita.registry.AllItems;

public record CarvingItem(
        String item,
        float erre,
        int anchorX,
        int anchorY
) {
    public int[][] shape() {
        return AllItems.getItem(item).getShape();
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
