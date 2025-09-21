package app.caquita.content.items.plastic.tools;

import app.caquita.content.items.ResourceType;
import app.caquita.content.items.ToolItemKind;
import app.caquita.content.items.classifications.PlasticClassification;

public class Fork implements ToolItemKind {

    @Override
    public int getMaxDurability() {
        return 50;
    }

    @Override
    public int[][] getIntensityArea() {
        return new int[][]{
                {0, 1, 0},
                {1, 2, 1},
                {0, 1, 0},
        };
    }

    @Override
    public String getId() {
        return "plastic:fork";
    }

    @Override
    public int[][] getShape() {
        return new int[][]{
                {1, 0},
                {1, 0},
        };
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PLASTIC;
    }

    @Override
    public String getClassification() {
        return String.valueOf(PlasticClassification.PET.code);
    }
}
