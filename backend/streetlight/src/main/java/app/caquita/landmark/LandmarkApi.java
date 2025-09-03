package app.caquita.landmark;

import app.caquita.auth.AuthUtils;
import app.caquita.storage.Database;
import io.javalin.http.Context;

import java.util.List;

public class LandmarkApi {

    public static void get(Context ctx, LandmarkType type) {
        AuthUtils.getUser(ctx);

        GetLandmarksPayload payload = ctx.bodyAsClass(GetLandmarksPayload.class);

        List<? extends Landmark> landmarks = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                        SELECT * FROM %s WHERE ST_DWithin(
                            position,
                            ST_MakePoint(:lon, :lat)::geography,
                            :visibility
                        )
                        """.formatted(type.getTableName()))
                        .bind("lat", payload.lat())
                        .bind("lon", payload.lon())
                        .bind("visibility", type.getVisibility())
                        .mapTo(type.getClazz())
                        .list()
        );

        ctx.status(200).json(landmarks);
    }

    record GetLandmarksPayload(double lat, double lon) {}
}
