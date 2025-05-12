package com.kryeit.content.items;

import com.kryeit.auth.inventory.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public final class ShapeUtils {

    public static List<InventoryItem.Cell> getOccupied(int col, int row, List<int[]> shape, InventoryItem.Orientation orientation) {
        List<InventoryItem.Cell> occupied = new ArrayList<>();
        List<int[]> oriented = applyOrientation(shape, orientation);
        for (int i = 0; i < oriented.size(); i++) {
            for (int j = 0; j < oriented.get(i).length; j++) {
                if (oriented.get(i)[j] == 1) {
                    occupied.add(new InventoryItem.Cell(col + j, row + i));
                }
            }
        }
        return occupied;
    }

    public static int getBoundingWidth(List<int[]> shape, InventoryItem.Orientation orientation) {
        List<int[]> oriented = applyOrientation(shape, orientation);
        int width = 0;
        for (int[] row : oriented) {
            for (int j = row.length - 1; j >= 0; j--) {
                if (row[j] == 1) {
                    width = Math.max(width, j + 1);
                    break;
                }
            }
        }
        return width;
    }

    public static int getBoundingHeight(List<int[]> shape, InventoryItem.Orientation orientation) {
        List<int[]> oriented = applyOrientation(shape, orientation);
        for (int i = oriented.size() - 1; i >= 0; i--) {
            for (int cell : oriented.get(i)) {
                if (cell == 1) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    private static List<int[]> applyOrientation(List<int[]> shape, InventoryItem.Orientation orientation) {
        return switch (orientation) {
            case UP -> shape;
            case RIGHT -> rotate(shape);
            case DOWN -> rotate(rotate(shape));
            case LEFT -> rotate(rotate(rotate(shape)));
        };
    }

    private static List<int[]> rotate(List<int[]> shape) {
        int h = shape.size();
        int w = shape.stream().mapToInt(r -> r.length).max().orElse(0);
        int[][] grid = new int[h][w];
        for (int i = 0; i < h; i++) {
            System.arraycopy(shape.get(i), 0, grid[i], 0, shape.get(i).length);
        }

        List<int[]> rotated = new ArrayList<>(w);
        for (int j = 0; j < w; j++) {
            int[] newRow = new int[h];
            for (int i = 0; i < h; i++) {
                newRow[i] = grid[h - 1 - i][j];
            }
            rotated.add(newRow);
        }
        return rotated;
    }
}
