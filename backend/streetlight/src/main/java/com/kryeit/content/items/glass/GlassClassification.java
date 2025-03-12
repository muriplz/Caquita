package com.kryeit.content.items.glass;

public enum GlassClassification {
    CLEAR("Clear/Flint Glass"),
    GREEN("Green Glass"),
    AMBER("Amber/Brown Glass"),
    BLUE("Blue Glass"),
    TEMPERED("Tempered/Safety Glass"),
    BOROSILICATE("Borosilicate/Heat-Resistant Glass"),
    MIXED("Mixed/Undefined Glass");

    private final String fullName;

    GlassClassification(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isRecyclable() {
        // Typically, colored container glass (clear, green, amber) is most recyclable
        // Tempered glass and mixed glass are often not accepted in standard recycling
        return this == CLEAR || this == GREEN || this == AMBER || this == BLUE;
    }

    public boolean isHighValueRecyclable() {
        // Clear glass is often preferred for recycling
        return this == CLEAR;
    }
}