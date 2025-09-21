package app.caquita.content.items.glass;

import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ResourceType;
import app.caquita.content.items.classifications.GlassClassification;

public class GlassBottle implements ItemKind {

    @Override
    public String id() {
        return "glass:bottle";
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
        return ResourceType.GLASS;
    }

    @Override
    public String classification() {
        return String.valueOf(GlassClassification.TEMPERED);
    }
}