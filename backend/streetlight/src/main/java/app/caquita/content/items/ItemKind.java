package app.caquita.content.items;

public interface ItemKind {
    String getId();

    int[][] getShape();

    default Rarity getRarity() {
        return Rarity.COMMON;
    }

    ResourceType getResourceType();

    String getClassification();

    enum Rarity {
        JUNK,
        COMMON,
        UNCOMMON,
        RARE,
        RELIC,
        ;
    }
}
