package app.caquita.content.items.classifications;

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
        return this == CLEAR || this == GREEN || this == AMBER || this == BLUE;
    }

    public boolean isHighValueRecyclable() {
        return this == CLEAR;
    }
}