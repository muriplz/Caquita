package app.caquita.auth.inventory.clothes.dusters;

import app.caquita.auth.inventory.clothes.Clothe;

public interface Duster extends Clothe {

    String getId();
    int[][] getInitialToolPocketsShape();

    abstract class DusterItemKind extends ClotheItemKind {


        @Override
        public int[][] getShape() {
            return new int[][]{
                    {1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 0}
            };
        }


    }
}
