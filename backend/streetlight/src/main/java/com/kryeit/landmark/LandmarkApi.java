package com.kryeit.landmark;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import io.javalin.http.Context;
import org.json.JSONObject;

import java.util.List;


public class LandmarkApi {

    /**
     * HTTP GET Request to /api/v1/landmarks
     * Returns a list of all landmarks.
     *
     * JSON Body Parameters:
     * - lat: The latitude of the user.
     * - lon: The longitude of the user.
     * - radius: The radius in meters.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void getLandmarks(Context ctx) {
        JSONObject body = new JSONObject(ctx.body());
        double lat = body.getDouble("lat");
        double lon = body.getDouble("lon");
        double radius = body.getDouble("radius");

        List<Landmark> landmarks = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM landmarks WHERE ST_Distance_Sphere(landmark_position, ST_MakePoint(:lon, :lat)) < :radius")
                        .bind("lat", lat)
                        .bind("lon", lon)
                        .bind("radius", radius)
                        .mapToBean(Landmark.class)
                        .list()
        );

        ctx.json(landmarks);
    }

    /**
     * HTTP GET Request to /api/v1/landmarks/{id}
     * Returns a landmark by id.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void getLandmark(Context ctx) {
        AuthUtils.getUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));

        Landmark landmark = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM landmarks WHERE id = :id")
                        .bind("id", id)
                        .mapToBean(Landmark.class)
                        .one()
        );

        ctx.json(landmark);
    }

    /**
     * HTTP PATCH Request to /api/v1/landmarks/{id}
     * Updates a landmark by id.
     *
     * JSON Body Parameters:
     * - name: The name of the landmark.
     * - lat: The latitude of the landmark.
     * - lon: The longitude of the landmark.
     * - type: The type of the landmark.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void updateLandmark(Context ctx) {
        long id = Long.parseLong(ctx.pathParam("id"));
        Landmark landmark = ctx.bodyAsClass(Landmark.class);

        String tableName = LandmarkType.TRASH_CAN.getTableName();

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE landmarks SET name = :name, landmark_position = ST_MakePoint(:lon, :lat), type = :type WHERE id = :id")
                        .bind("name", landmark.name())
                        .bind("lat", landmark.lat())
                        .bind("lon", landmark.lon())
                        .bind("type", landmark.type().name())
                        .bind("id", id)
                        .execute()
        );

        ctx.status(204).result("Landmark updated successfully.");
    }

    public static void getTypes(Context ctx) {
        List<LandmarkType> types = List.of(LandmarkType.values());

        ctx.json(types);
    }
}
