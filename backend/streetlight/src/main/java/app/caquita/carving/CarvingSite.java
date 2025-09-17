package app.caquita.carving;

import java.util.List;

public interface CarvingSite {


    List<String> carvables();
    List<String> obstacles();

    SiteType type();

    int width();
    int height();

    CarvingCell[][] getLayout();

    default void carve(int x, int y) {
        CarvingCell current = getLayout()[y][x];
        String currentCarvable = current.carvable();

        if (carvables().contains(currentCarvable)) {
            int index = carvables().indexOf(currentCarvable);
            String newCarvable;

            if (index < carvables().size() - 1) {
                newCarvable = carvables().get(index + 1);
            } else {
                newCarvable = "dirt:empty";
            }

            getLayout()[y][x] = new CarvingCell(newCarvable, current.obstacle());
        }
    }

    public enum SiteType {
        DIRT,
        ;
    }

    public record CarvingCell(
            String carvable,
            String obstacle
    ) {}
}
