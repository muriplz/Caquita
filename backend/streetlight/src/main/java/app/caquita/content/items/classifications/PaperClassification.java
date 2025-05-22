package app.caquita.content.items.classifications;

public enum PaperClassification {
    NEWSPRINT("Newsprint/Newspaper"),
    OFFICE("Office/Writing Paper"),
    MAGAZINE("Magazine/Glossy Paper"),
    MIXED("Mixed Paper"),
    THERMAL("Thermal Paper"),
    SHREDDED("Shredded Paper"),
    LAMINATED("Laminated Paper");

    private final String name;

    PaperClassification(String name) {
        this.name = name;
    }

}