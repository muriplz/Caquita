package app.caquita.content.items.cardboard;

import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ResourceType;
import app.caquita.content.items.classifications.CardboardClassification;

public class PizzaBox implements ItemKind {

    @Override
    public String getId() {
        return "cardboard:pizza_box";
    }

    @Override
    public int[][] getShape() {
        return new int[][]{
                {0, 1},
                {1, 1}
        };
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.CARDBOARD;
    }

    @Override
    public String getClassification() {
        return String.valueOf(CardboardClassification.MIXED.getFullName());
    }
}