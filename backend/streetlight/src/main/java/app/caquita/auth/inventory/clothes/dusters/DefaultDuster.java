package app.caquita.auth.inventory.clothes.dusters;

public class DefaultDuster implements Duster {
    @Override
    public String getId() {
        return "duster:default";
    }

    @Override
    public int[][] getInitialToolPocketsShape() {
        return new int[][]{
                {1, 0},
                {0, 0}
        };
    }

    public static class ItemKind extends DusterItemKind {

        @Override
        public String getId() { return "duster:default"; }
    }
}
