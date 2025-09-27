package app.caquita.auth.inventory.clothes.dusters;

import javax.annotation.Nullable;

public record EquippedDuster(
        String duster,
        ToolData equippedTool, // null if no tool equipped
        int pockets, // If > non-null pockets, then we let the user add more pockets
        ToolPocket[][] toolPocketsShape // Something = pocket, null = no pocket
) {


    public record ToolPocket(boolean locked, ToolData tool) {}

    @Nullable
    public record ToolData(String toolId, int durability, float erre) {}
}
