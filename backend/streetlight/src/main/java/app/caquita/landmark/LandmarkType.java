package app.caquita.landmark;

import app.caquita.landmark.trash_can.TrashCan;

public enum LandmarkType {
    TRASH_CAN(TrashCan.class, "trash_cans", 50),
    ;

    private Class<? extends Landmark> clazz;
    private String tableName;
    private int visibility;

    LandmarkType(Class<? extends Landmark> clazz, String tableName, int visibility) {
        this.clazz = clazz;
        this.tableName = tableName;
        this.visibility = visibility;
    }

    public String getTableName() {
        return tableName;
    }

    public Class<? extends Landmark> getClazz() {
        return clazz;
    }

    public int getVisibility() {
        return visibility;
    }
}
