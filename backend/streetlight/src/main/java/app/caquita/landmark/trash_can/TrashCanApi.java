package app.caquita.landmark.trash_can;

import app.caquita.Database;
import app.caquita.landmark.LandmarkApi;
import app.caquita.landmark.LandmarkType;
import io.javalin.http.Context;
import org.json.JSONObject;

public class TrashCanApi {

    public static void get(Context ctx) {
        LandmarkApi.get(ctx, LandmarkType.TRASH_CAN);
    }

    public static void create(double lat, double lon, String name, String description, long author,
                              JSONObject features) {

        boolean broken = features.optBoolean("broken", false);
        boolean ashtray = features.optBoolean("ashtray", false);
        boolean windblown = features.optBoolean("windblown", false);
        boolean flooded = features.optBoolean("flooded", false);
        boolean overwhelmed = features.optBoolean("overwhelmed", false);
        boolean poopbag = features.optBoolean("poopbags", false);
        boolean art = features.optBoolean("art", false);

        Database.getJdbi().withHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO trash_cans (
                            position,
                            name,
                            description,
                            author,
                            broken,
                            ashtray,
                            windblown,
                            flooded,
                            overwhelmed,
                            poopbags,
                            art
                        )
                        VALUES (
                            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                            :name,
                            :description,
                            :author,
                            :broken,
                            :ashtray,
                            :windblown,
                            :flooded,
                            :overwhelmed,
                            :poopbags,
                            :art
                        )
                        """)
                        .bind("lat", lat)
                        .bind("lon", lon)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("author", author)
                        .bind("broken", broken)
                        .bind("ashtray", ashtray)
                        .bind("windblown", windblown)
                        .bind("flooded", flooded)
                        .bind("overwhelmed", overwhelmed)
                        .bind("poopbags", poopbag)
                        .bind("art", art)
                        .execute()
        );
    }

}
