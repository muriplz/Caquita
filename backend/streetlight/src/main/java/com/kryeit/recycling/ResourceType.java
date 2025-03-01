package com.kryeit.recycling;

public enum ResourceType {
    PLASTIC,
    METAL,
    GLASS,
    PAPER,
    ORGANIC,
    JUNK,
    OTHER,
    ;

    public static ResourceType fromFolderName(String folderName) {
        return switch(folderName.toLowerCase()) {
            case "plastic" -> PLASTIC;
            case "metal" -> METAL;
            case "glass" -> GLASS;
            case "paper" -> PAPER;
            case "organic" -> ORGANIC;
            case "junk" -> JUNK;
            default -> OTHER;
        };
    }
}