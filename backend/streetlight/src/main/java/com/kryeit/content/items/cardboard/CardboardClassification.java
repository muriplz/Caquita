package com.kryeit.content.items.cardboard;

import com.kryeit.content.items.MaterialClassification;

public enum CardboardClassification implements MaterialClassification {
    CORRUGATED("Corrugated Cardboard"),
    PAPERBOARD("Paperboard/Boxboard"),
    BEVERAGE("Beverage Cartons"),
    FOOD_GRADE("Food Grade Cardboard"),
    COATED("Coated/Waxed Cardboard"),
    MIXED("Mixed Cardboard");

    private final String fullName;

    CardboardClassification(String fullName) {
        this.fullName = fullName;
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

    public boolean isRecyclable() {
        return this != COATED;
    }

    public boolean isHighValueRecyclable() {
        return this == CORRUGATED;
    }
}