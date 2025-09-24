package app.caquita.auth.inventory.clothes.dusters;

public record EquippedDuster(
        String duster,
        ToolData equippedTool, // null if no tool equipped
        int pockets, // If > non-null pockets, then we let the user add more pockets
        ToolPocket[][] toolPocketsShape // Something = pocket, null = no pocket
) {


    public record ToolPocket(int col, int row, ToolData tool) {}
    public record ToolData(String toolId, int durability, float erre) {}
}
