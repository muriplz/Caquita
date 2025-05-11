package com.kryeit.content.items.classifications;

import com.kryeit.content.items.MaterialClassification;

public enum PlasticClassification implements MaterialClassification {
    PET(1, "Polyethylene Terephthalate"),
    HDPE(2, "High-Density Polyethylene"),
    PVC(3, "Polyvinyl Chloride"),
    LDPE(4, "Low-Density Polyethylene"),
    PP(5, "Polypropylene"),
    PS(6, "Polystyrene"),
    OTHER(7, "Other/Mixed Plastics");

    private final int recyclingCode;
    private final String fullName;

    PlasticClassification(int recyclingCode, String fullName) {
        this.recyclingCode = recyclingCode;
        this.fullName = fullName;
    }

    public int getRecyclingCode() {
        return recyclingCode;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String getId() {
        return name();
    }

    @Override
    public String getName() {
        return fullName;
    }

    @Override
    public Integer getCode() {
        return recyclingCode;
    }

    public boolean isRecyclable() {
        return this != PVC && this != OTHER;
    }

    public static PlasticClassification fromRecyclingCode(int code) {
        for (PlasticClassification type : values()) {
            if (type.recyclingCode == code) {
                return type;
            }
        }
        return OTHER;
    }
}