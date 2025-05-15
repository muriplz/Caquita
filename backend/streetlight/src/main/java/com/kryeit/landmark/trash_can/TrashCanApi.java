package com.kryeit.landmark.trash_can;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.kryeit.auth.TrustLevel;
import com.kryeit.landmark.LandmarkApi;
import com.kryeit.landmark.LandmarkType;
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
        boolean poopbag = features.optBoolean("poopbag", false);
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
                            poopbag,
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
                            :poopbag,
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
                        .bind("poopbag", poopbag)
                        .bind("art", art)
                        .execute()
        );
    }

    record CreateTrashCanPayload(

    ) {}
}
