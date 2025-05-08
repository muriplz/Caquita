package com.kryeit.content.items.paper;

import com.kryeit.content.items.MaterialClassification;

public enum PaperClassification implements MaterialClassification {
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

    @Override
    public String getId() {
        return name();
    }

    @Override
    public String getName() {
        return fullName;
    }

    public boolean isRecyclable() {
        return this != THERMAL && this != LAMINATED;
    }

    public boolean isHighValueRecyclable() {
        return this == OFFICE;
    }
}