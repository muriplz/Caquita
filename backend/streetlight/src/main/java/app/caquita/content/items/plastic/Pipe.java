package app.caquita.content.items.plastic;

import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ResourceType;
import app.caquita.content.items.classifications.PlasticClassification;

public class Pipe implements ItemKind {

    @Override
    public String getId() {
        return "plastic:pipe";
    }

    @Override
    public int[][] getShape() {
        return new int[][]{
                {1, 1, 1},
                {1, 0, 0},
                {0, 0, 0},
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