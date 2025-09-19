// CarveSiteGenerator.java
package app.caquita.carving;

public class CarveSiteGenerator {

    public static CarvingSite generateSiteForLocation(long seed, double lat, double lon) {
        return new DirtSite(seed);
    }

    public static DirtSite generateNewDirtSite(long seed) {
        return new DirtSite(seed);
    }

    public static DirtSite generateNewDirtSite() {
        return new DirtSite(System.currentTimeMillis());
    }
}