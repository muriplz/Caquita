package com.kryeit.landmark;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import io.javalin.http.Context;
import org.json.JSONObject;

import java.util.List;


public class LandmarkApi {

    public static long create(String name, double lat, double lon, LandmarkType type, String data) {
        return Database.getJdbi().inTransaction(handle -> {
            handle.createUpdate("""
            INSERT INTO landmarks (name, position, type)
            VALUES (:name, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :type)
            """)
                    .bind("name", name)
                    .bind("lat", lat)
                    .bind("lon", lon)
                    .bind("type", type.name())
                    .execute();

            long id = handle.createQuery("SELECT lastval()")
                    .mapTo(Long.class)
                    .one();

            switch (type) {
                case TRASH_CAN -> handle.createUpdate("""
                INSERT INTO trash_cans (id, type, features)
                VALUES (:id, :type, cast(:features as jsonb))
                """)
                        .bind("id", id)
                        .bind("type", LandmarkType.TRASH_CAN.name())
                        .bind("features", data)
                        .execute();

                case PLASTIC_CONTAINER -> handle.createUpdate("""
                INSERT INTO plastic_containers (id, type, features)
                VALUES (:id, :type, cast(:features as jsonb))
                """)
                        .bind("id", id)
                        .bind("type", LandmarkType.PLASTIC_CONTAINER.name())
                        .bind("features", data)
                        .execute();

                // Add cases for other landmark types as needed
            }

            return id;
        });
    }

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
                handle.createQuery("""
                    SELECT id, name,
                           ST_X(position::geometry) as lon,
                           ST_Y(position::geometry) as lat,
                           type
                    FROM landmarks
                    WHERE ST_DWithin(
                        position,
                        ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                        :radius
                    )
                    """)
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
                handle.createQuery("""
                    SELECT id, name,
                           ST_X(position::geometry) as lon,
                           ST_Y(position::geometry) as lat,
                           type
                    FROM landmarks
                    WHERE id = :id
                    """)
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
                handle.createUpdate("""
                    UPDATE landmarks
                    SET name = :name,
                        position = ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                        type = :type
                    WHERE id = :id
                    """)
                        .bind("name", landmark.name())
                        .bind("lat", landmark.lat())
                        .bind("lon", landmark.lon())
                        .bind("type", landmark.type().name())
                        .bind("id", id)
                        .execute()
        );

        ctx.status(204).result("Landmark updated successfully.");
    }
}
