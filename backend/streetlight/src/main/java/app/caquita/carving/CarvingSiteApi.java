// CarvingSiteApi.java
package app.caquita.carving;

import app.caquita.auth.AuthUtils;
import io.javalin.http.Context;
import java.util.concurrent.ConcurrentHashMap;

public class CarvingSiteApi {

    private static final ConcurrentHashMap<Long, CarvingSite> userSites = new ConcurrentHashMap<>();

    public static void generate(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        GenerateCarvingSitePayload payload = ctx.bodyAsClass(GenerateCarvingSitePayload.class);

        if (!isValidLocation(payload.lat(), payload.lon())) {
            ctx.status(400).json(new ErrorResponse("Invalid location"));
            return;
        }

        long siteId = generateSiteId(userId, payload.lat(), payload.lon());
        CarvingSite site = userSites.computeIfAbsent(siteId, k ->
                generateSiteForLocation(siteId, payload.lat(), payload.lon()));

        ctx.json(new CarvingSiteResponse(siteId, site));
    }

    public static void carve(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        long siteId = ctx.pathParamAsClass("siteId", Long.class).get();
        CarvePayload payload = ctx.bodyAsClass(CarvePayload.class);

        CarvingSite site = userSites.get(siteId);
        if (site == null) {
            ctx.status(404).json(new ErrorResponse("Site not found"));
            return;
        }

        if (payload.shape() != null) {
            site.carve(payload.shape());
        } else {
            site.carve(payload.x(), payload.y());
        }

        ctx.json(new CarvingSiteResponse(siteId, site));
    }

    public static void getSite(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        long siteId = ctx.pathParamAsClass("siteId", Long.class).get();

        CarvingSite site = userSites.get(siteId);
        if (site == null) {
            ctx.status(404).json(new ErrorResponse("Site not found"));
            return;
        }

        ctx.json(new CarvingSiteResponse(siteId, site));
    }

    private static CarvingSite generateSiteForLocation(long siteId, double lat, double lon) {
        return CarveSiteGenerator.generateSiteForLocation(siteId, lat, lon);
    }

    private static boolean isValidLocation(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    private static long generateSiteId(long userId, double lat, double lon) {
        int gridLat = (int) Math.floor(lat * 1000);
        int gridLon = (int) Math.floor(lon * 1000);
        return ((long) gridLat << 32) | (gridLon & 0xffffffffL);
    }

    record GenerateCarvingSitePayload(double lat, double lon) {}

    record CarvePayload(int x, int y, int[][] shape) {}

    record CarvingSiteResponse(long siteId, CarvingSite site) {}

    record ErrorResponse(String error) {}
}