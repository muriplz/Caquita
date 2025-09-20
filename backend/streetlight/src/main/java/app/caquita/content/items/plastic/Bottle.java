package app.caquita.content.items.plastic;

import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ResourceType;
import app.caquita.content.items.classifications.PlasticClassification;

import java.util.List;

public class Bottle implements ItemKind {

    @Override
    public String id() {
        return "plastic:bottle";
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