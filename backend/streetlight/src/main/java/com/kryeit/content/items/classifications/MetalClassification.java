package com.kryeit.content.items.classifications;

public enum MetalClassification {
    ALUMINUM("Aluminum"),
    STEEL("Steel/Tin"),
    COPPER("Copper"),
    BRASS("Brass"),
    STAINLESS_STEEL("Stainless Steel"),
    LEAD("Lead"),
    ZINC("Zinc"),
    MIXED("Mixed Metals"),
    E_WASTE("Electronic Waste Metals"),
    PRECIOUS("Precious Metals");

    private final String fullName;

    MetalClassification(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }



    public boolean isRecyclable() {
        return true;
    }

    public boolean isHighValueRecyclable() {
        return this == ALUMINUM || this == COPPER || this == BRASS || this == PRECIOUS;
    }

    public boolean isHazardous() {
        return this == LEAD || this == E_WASTE;
    }

    public boolean requiresSpecialProcessing() {
        return this == E_WASTE || this == MIXED || this == PRECIOUS;
    }
}