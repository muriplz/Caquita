package app.caquita.content.items.plastic.tools;

import app.caquita.content.items.ResourceType;
import app.caquita.content.items.ToolItemKind;
import app.caquita.content.items.classifications.PlasticClassification;

public class Fork implements ToolItemKind {

    @Override
    public int maxDurability() {
        return 50;
    }

    @Override
    public int[][] intensityArea() {
        return new int[][]{
                {0, 1, 0},
                {1, 2, 1},
                {0, 1, 0},
        };
    }

    @Override
    public String id() {
        return "plastic:fork";
    }

    @Override
    public int[][] shape() {
        return new int[][]{
                {1, 0},
                {1, 0},
        };
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.PLASTIC;
    }

    @Override
    public String classification() {
        return String.valueOf(PlasticClassification.PET.code);
    }
}
