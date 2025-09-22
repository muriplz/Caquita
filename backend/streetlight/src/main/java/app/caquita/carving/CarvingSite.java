package app.caquita.carving;

import app.caquita.carving.obstacles.CarvingObstacle;
import app.caquita.carving.obstacles.CarvingObstacleInstance;
import app.caquita.content.items.ToolItemKind;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public interface CarvingSite {

    @JsonProperty("carvables")
    List<String> carvables();

    @JsonProperty("obstacles")
    List<CarvingObstacle> obstacles();

    @JsonProperty("itemPool")
    List<String> itemPool();

    @JsonProperty("type")
    SiteType type();

    @JsonProperty("width")
    int width();

    @JsonProperty("height")
    int height();

    @JsonProperty("layout")
    CarvingCell[][] layout();

    @JsonProperty("placedObstacles")
    List<CarvingObstacleInstance> placedObstacles();

    @JsonProperty("placedItems")
    List<CarvingItem> placedItems();

    default void carveCell(ToolItemKind tool, int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            return;
        }

        CarvingCell current = layout()[y][x];
        String currentCarvable = current.carvable();

        CarvingObstacleInstance obstacle = getObstacleAt(x, y);
        CarvingItem item = getItemAt(x, y);



        if ("empty".equals(currentCarvable)) {
            if (obstacle != null) {
                if (!tool.isGentle()) {
                    obstacles().stream()
                            .filter(o -> o.id().equals(obstacle.obstacle()))
                            .findFirst()
                            .ifPresent(CarvingObstacle::trigger);
                }
                return;
            }
            
            if (item != null) {
                float newErre = item.erre() - 0.1f;

                if (newErre <= 0.0f) {
                    placedItems().remove(item);
                } else {
                    CarvingItem updatedItem = new CarvingItem(item.item(), newErre, item.anchorX(), item.anchorY());
                    placedItems().set(placedItems().indexOf(item), updatedItem);
                }
            }
            return;
        }

        if (!carvables().contains(currentCarvable)) {
            return;
        }

        int index = carvables().indexOf(currentCarvable);
        String newCarvable = (index > 0) ? carvables().get(index - 1) : "empty";

        layout()[y][x] = new CarvingCell(newCarvable);
    }

    default void carveGroup(ToolItemKind tool, int x, int y) {
        int[][] intensityArea = tool.getIntensityArea();

        // Calculate center offset
        int centerX = intensityArea[0].length / 2;
        int centerY = intensityArea.length / 2;

        for (int dy = 0; dy < intensityArea.length; dy++) {
            for (int dx = 0; dx < intensityArea[dy].length; dx++) {
                int intensity = intensityArea[dy][dx];

                if (intensity > 0) {
                    // Apply center offset so clicked position is the center
                    int targetX = x + dx - centerX;
                    int targetY = y + dy - centerY;

                    for (int i = 0; i < intensity; i++) {
                        carveCell(tool, targetX, targetY);
                    }
                }
            }
        }
    }

    default CarvingObstacleInstance getObstacleAt(int x, int y) {
        return placedObstacles().stream()
                .filter(obs -> obs.coversCell(x, y))
                .findFirst()
                .orElse(null);
    }

    default CarvingItem getItemAt(int x, int y) {
        return placedItems().stream()
                .filter(item -> item.coversCell(x, y))
                .findFirst()
                .orElse(null);
    }

    enum SiteType {
        DIRT,
        ;
    }

    record CarvingCell(String carvable) {
        public boolean isFullyCarved() {
            return "empty".equals(carvable);
        }
    }
}