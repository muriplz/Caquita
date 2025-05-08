package com.kryeit.recycling;

public enum ResourceType {
    PLASTIC,
    METAL,
    GLASS,
    PAPER,
    CARDBOARD,
    GREASY_CARDBOARD,
    OIL,
    ORGANIC,
    OTHER,
    ;

    public static ResourceType fromFolderName(String folderName) {
        return switch(folderName.toLowerCase()) {
            case "plastic" -> PLASTIC;
            case "metal" -> METAL;
            case "glass" -> GLASS;
            case "paper" -> PAPER;
            case "cardboard" -> CARDBOARD;
            case "oil" -> OIL;
            case "organic" -> ORGANIC;
            default -> OTHER;
        };
    }
}