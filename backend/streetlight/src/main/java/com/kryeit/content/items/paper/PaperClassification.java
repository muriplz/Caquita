package com.kryeit.content.items.paper;

public enum PaperClassification {
    NEWSPRINT("Newsprint/Newspaper"),
    OFFICE("Office/Writing Paper"),
    MAGAZINE("Magazine/Glossy Paper"),
    MIXED("Mixed Paper"),
    THERMAL("Thermal Paper"),
    SHREDDED("Shredded Paper"),
    LAMINATED("Laminated Paper");

    private final String fullName;

    PaperClassification(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isRecyclable() {
        // Most paper types are recyclable except thermal and laminated paper
        return this != THERMAL && this != LAMINATED;
    }

    public boolean isHighValueRecyclable() {
        // Office paper is typically more valuable for recycling
        return this == OFFICE;
    }
}