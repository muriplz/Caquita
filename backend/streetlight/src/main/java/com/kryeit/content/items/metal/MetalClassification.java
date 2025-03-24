package com.kryeit.content.items.metal;

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
        // Most metals are recyclable
        return true;
    }

    public boolean isHighValueRecyclable() {
        // Aluminum, copper, brass, and precious metals typically have higher recycling value
        return this == ALUMINUM || this == COPPER || this == BRASS || this == PRECIOUS;
    }

    public boolean isHazardous() {
        // Lead and some e-waste metals require special handling
        return this == LEAD || this == E_WASTE;
    }

    public boolean requiresSpecialProcessing() {
        // E-waste metals, mixed metals and some others may require special processing
        return this == E_WASTE || this == MIXED || this == PRECIOUS;
    }
}