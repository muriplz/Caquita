package com.kryeit.landmark;

public enum LandmarkType {
    TRASH_CAN,
    ;

    public String getTableName() {
        return switch (this) {
            case TRASH_CAN -> "cans";
        };
    }
}
