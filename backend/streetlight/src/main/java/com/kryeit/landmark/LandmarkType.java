package com.kryeit.landmark;

public enum LandmarkType {
    TRASH_CAN,
    PLASTIC,
    GLASS,
    PAPER,
    ;

    public String getTableName() {
        return switch (this) {
            case TRASH_CAN -> "cans";
            case PLASTIC -> "plastic";
            case GLASS -> "glass";
            case PAPER -> "paper";
        };
    }
}
