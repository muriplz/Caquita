package app.caquita.content.items;

public interface ToolItemKind extends ItemKind {

    int getMaxDurability();

    int[][] getActionShape();
}
