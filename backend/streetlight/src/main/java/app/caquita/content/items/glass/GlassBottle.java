package app.caquita.content.items.glass;

import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ResourceType;
import app.caquita.content.items.classifications.GlassClassification;

import java.util.List;

public class GlassBottle implements ItemKind {

    @Override
    public String getId() {
        return "glass:bottle";
    }

    @Override
    public List<int[]> getShape() {
        return List.of(
                new int[]{1, 0},
                new int[]{1, 0}
        );
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.GLASS;
    }

    @Override
    public String getClassification() {
        return String.valueOf(GlassClassification.TEMPERED);
    }
}