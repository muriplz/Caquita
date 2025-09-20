// CarvingSite.java
package app.caquita.carving;

import app.caquita.carving.obstacles.CarvingObstacle;
import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ToolItemKind;
import app.caquita.registry.AllItems;
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

    default void carveCell(ToolItemKind tool, int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            return;
        }

        CarvingCell current = layout()[y][x];
        String currentCarvable = current.carvable();

        System.out.println("DEBUG: Carving at (" + x + ", " + y + ") - Current: " + currentCarvable +
                ", Obstacle: " + current.obstacle() + ", Item: " +
                (current.buriedItem != null ? current.buriedItem.item() : "none"));

        if (current.hasObstacle()) {
            CarvingObstacle obstacle = obstacles().stream()
                    .filter(o -> o.id().equals(current.obstacle()))
                    .findFirst()
                    .orElse(null);

            if (!tool.gentle() && obstacle != null) obstacle.trigger();
            System.out.println("DEBUG: Hit obstacle '" + current.obstacle() + "' at (" + x + ", " + y + ")");
            return;
        }

        if ("empty".equals(currentCarvable)) {
            System.out.println("DEBUG: Already fully carved at (" + x + ", " + y + ")");
            if (current.hasBuriedItem()) {
                CarvingItem item = current.buriedItem;
                float newErre = item.erre() - 0.1f;
                System.out.println("DEBUG: Hit buried item '" + item.item() +
                        "', erre: " + item.erre() + " -> " + newErre);

                if (newErre <= 0.0f) {
                    System.out.println("DEBUG: Item fully excavated!");
                    layout()[y][x] = new CarvingCell(currentCarvable, current.obstacle(), null);
                } else {
                    CarvingItem updatedItem = new CarvingItem(item.item(), newErre);
                    layout()[y][x] = new CarvingCell(currentCarvable, current.obstacle(), updatedItem);
                }
            }
            return;
        }

        if (!carvables().contains(currentCarvable)) {
            System.out.println("DEBUG: Unknown carvable type '" + currentCarvable + "' at (" + x + ", " + y + ")");
            return;
        }

        int index = carvables().indexOf(currentCarvable);
        String newCarvable = (index > 0)
                ? carvables().get(index - 1)
                : "empty";

        System.out.println("DEBUG: Carving '" + currentCarvable + "' -> '" + newCarvable + "'");

        layout()[y][x] = new CarvingCell(newCarvable, current.obstacle(), current.buriedItem());

        if ("empty".equals(newCarvable) && current.buriedItem() != null) {
            System.out.println("DEBUG: Revealing buried item '" + current.buriedItem().item() + "' at (" + x + ", " + y + ")");
        }
    }

    default void carveGroup(ToolItemKind tool, int x, int y) {
        int[][] intensityArea = tool.intensityArea();

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

    enum SiteType {
        DIRT,
        ;
    }

    record CarvingCell(
            String carvable,
            String obstacle,
            CarvingItem buriedItem
    ) {
        public CarvingCell(String carvable) {
            this(carvable, null, null);
        }

        public boolean hasObstacle() {
            return obstacle != null;
        }

        public boolean hasBuriedItem() {
            return buriedItem != null;
        }

        public boolean isFullyCarved() {
            return "empty".equals(carvable);
        }
    }
}