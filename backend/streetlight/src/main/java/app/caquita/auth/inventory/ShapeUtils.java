package app.caquita.auth.inventory;

import java.util.ArrayList;
import java.util.List;

public final class ShapeUtils {

    public static List<InventoryItem.Cell> getOccupied(int col, int row, int[][] shape) {
        List<InventoryItem.Cell> occupied = new ArrayList<>();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    occupied.add(new InventoryItem.Cell(col + j, row + i));
                }
            }
        }
        return occupied;
    }

    public static int getBoundingWidth(int[][] shape) {
        int width = 0;
        for (int[] row : shape) {
            for (int j = row.length - 1; j >= 0; j--) {
                if (row[j] == 1) {
                    width = Math.max(width, j + 1);
                    break;
                }
            }
        }
        return width;
    }

    public static int getBoundingHeight(int[][] shape) {
        for (int i = shape.length - 1; i >= 0; i--) {
            for (int cell : shape[i]) {
                if (cell == 1) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    public static InventoryItem.Cell getTopLeftOccupied(int[][] shape) {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;

        for (int i = 0; i < shape.length; i++) {
            int[] row = shape[i];
            for (int j = 0; j < row.length; j++) {
                if (row[j] == 1) {
                    minRow = Math.min(minRow, i);
                    minCol = Math.min(minCol, j);
                }
            }
        }

        return new InventoryItem.Cell(minCol, minRow);
    }

    public static InventoryItem.Cell anchorToPlacement(int anchorCol, int anchorRow, int[][] shape) {
        InventoryItem.Cell topLeft = getTopLeftOccupied(shape);
        return new InventoryItem.Cell(anchorCol - topLeft.col(), anchorRow - topLeft.row());
    }
}