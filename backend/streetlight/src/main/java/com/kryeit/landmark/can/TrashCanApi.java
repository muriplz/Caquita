package com.kryeit.landmark.can;

import com.kryeit.Database;
import com.kryeit.landmark.Landmark;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.ResourceType;
import io.javalin.http.Context;
import org.json.JSONObject;

import java.util.List;

public class TrashCanApi {

    /**
     * HTTP GET Request to /api/v1/landmarks/cans
     * Returns a list of all cans.
     *
     * JSON Body Parameters:
     * - lat: The latitude of the user.
     * - lon: The longitude of the user.
     * - radius: The radius in meters.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void getCans(Context ctx) {
        JSONObject body = new JSONObject(ctx.body());
        double lat = body.getDouble("lat");
        double lon = body.getDouble("lon");
        double radius = body.getDouble("radius");

        List<Landmark> landmarks = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                    SELECT * FROM landmarks
                    WHERE id IN (SELECT landmark_id FROM cans)
                    AND ST_Distance_Sphere(landmark_position, ST_MakePoint(:lon, :lat)) < :radius
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
     * HTTP POST Request to /api/v1/landmarks/cans
     * Creates a new can.
     *
     * JSON Body Parameters:
     * - name: The name of the can.
     * - lat: The latitude of the can.
     * - lon: The longitude of the can.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void createCan(Context ctx) {
        JSONObject body = new JSONObject(ctx.body());
        String name = body.getString("name");
        double lat = body.getDouble("lat");
        double lon = body.getDouble("lon");

        long id = Database.getJdbi().withHandle(handle -> {
            handle.createUpdate("""
                INSERT INTO landmarks (landmark_position)
                VALUES (:name, ST_MakePoint(:lon, :lat), :type)
                """)
                    .bind("name", name)
                    .bind("lat", lat)
                    .bind("lon", lon)
                    .bind("type", LandmarkType.TRASH_CAN)
                    .execute();

            return handle.createQuery("SELECT LAST_INSERT_ID()")
                    .mapTo(Long.class)
                    .one();
        });

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO cans (id, type, features)
                        VALUES (:id, :type, :features)
                        """)
                        .bind("id", id)
                        .bind("type", ResourceType.OTHER)
                        .bind("features", List.of())
                        .execute()
        );

        ctx.status(201).result("Created");
    }
}
