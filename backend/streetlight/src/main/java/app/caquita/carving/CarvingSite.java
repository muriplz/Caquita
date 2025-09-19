// CarvingSite.java
package app.caquita.carving;

import app.caquita.carving.obstacles.CarvingObstacle;
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

    default void carve(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            return;
        }

        CarvingCell current = layout()[y][x];
        String currentCarvable = current.carvable();

        if (carvables().contains(currentCarvable)) {
            int index = carvables().indexOf(currentCarvable);
            String newCarvable = (index < carvables().size() - 1)
                    ? carvables().get(index + 1)
                    : "empty";

            layout()[y][x] = new CarvingCell(newCarvable, current.obstacle(), current.buriedItem());

            if (newCarvable.equals("empty") && current.buriedItem() != null) {
                layout()[y][x] = new CarvingCell(newCarvable, current.obstacle(), null);
            }
        }
    }

    default void carve(int[][] shape) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    carve(j, i);
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
            String buriedItem
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