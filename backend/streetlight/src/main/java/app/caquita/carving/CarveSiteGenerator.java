package app.caquita.carving;

public class CarveSiteGenerator {

    public static CarvingSite generateSite(long seed, SiteGenerator.GenerationParams params) {
        return new DirtSite(seed, params);
    }
}