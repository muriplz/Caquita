package com.kryeit.landmark;

public enum LandmarkType {
    TRASH_CAN,
    PLASTIC_CONTAINER,
    GLASS,
    PAPER,
    METAL,
    ;

    public String getTableName() {
        return switch (this) {
            case TRASH_CAN -> "trash_cans";
            case PLASTIC_CONTAINER -> "plastic_containers";
            case GLASS -> "glass";
            case PAPER -> "paper";
            case METAL -> "metal";
        };
    }
}
