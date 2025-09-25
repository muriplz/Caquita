package app.caquita.auth.inventory.clothes;

import app.caquita.auth.inventory.clothes.dusters.Duster;
import app.caquita.content.items.ItemKind;
import app.caquita.content.items.ResourceType;
import app.caquita.registry.AllClothes;

public interface Clothe {
    String getId();

    abstract class ClotheItemKind implements ItemKind {

        @Override
        public abstract String getId();

        @Override
        public abstract int[][] getShape();


        @Override
        public ResourceType getResourceType() {
            return ResourceType.FABRIC;
        }

        @Override
        public String getClassification() {
            return null;
        }

        public Duster toDuster() {
            return (Duster) AllClothes.getClothe(getId());
        }
    }
}
