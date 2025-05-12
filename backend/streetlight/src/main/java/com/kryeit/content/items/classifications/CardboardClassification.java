package com.kryeit.content.items.classifications;

public enum CardboardClassification {
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


    public boolean isRecyclable() {
        return this != COATED;
    }

    public boolean isHighValueRecyclable() {
        return this == CORRUGATED;
    }
}