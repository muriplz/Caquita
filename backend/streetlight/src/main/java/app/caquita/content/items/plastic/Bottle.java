package app.caquita.content.items.plastic;

import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ResourceType;
import app.caquita.content.items.classifications.PlasticClassification;

import java.util.List;

public class Bottle implements ItemKind {

    @Override
    public String getId() {
        return "plastic:bottle";
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
        return ResourceType.PLASTIC;
    }

    @Override
    public String getClassification() {
        return String.valueOf(PlasticClassification.PET.code);
    }
}