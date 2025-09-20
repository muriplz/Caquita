package app.caquita.content.items;

public interface ItemKind {
    String id();

    int[][] shape();

    default Rarity rarity() {
        return Rarity.COMMON;
    }

    ResourceType resourceType();

    String classification();

    enum Rarity {
        JUNK,
        COMMON,
        UNCOMMON,
        RARE,
        RELIC,
        ;
    }
}
