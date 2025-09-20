package app.caquita.content.items;

public interface ToolItemKind extends ItemKind {

    int maxDurability();

    int[][] intensityArea();

    default boolean gentle() {
        return false;
    }
}
