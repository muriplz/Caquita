package app.caquita.content.items.classifications;

public enum PlasticClassification {
    PET(1, "Polyethylene Terephthalate"),
    HDPE(2, "High-Density Polyethylene"),
    PVC(3, "Polyvinyl Chloride"),
    LDPE(4, "Low-Density Polyethylene"),
    PP(5, "Polypropylene"),
    PS(6, "Polystyrene"),
    OTHER(7, "Other/Mixed Plastics");

    public final int code;
    public final String name;

    PlasticClassification(int code, String name) {
        this.code = code;
        this.name = name;
    }
}