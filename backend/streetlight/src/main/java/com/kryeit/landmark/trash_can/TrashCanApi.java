package com.kryeit.landmark.trash_can;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.kryeit.auth.TrustLevel;
import com.kryeit.landmark.LandmarkApi;
import com.kryeit.landmark.LandmarkType;
import io.javalin.http.Context;

public class TrashCanApi {

    public static void get(Context ctx) {
        LandmarkApi.get(ctx, LandmarkType.TRASH_CAN);
    }

    public static void create(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        if (!AuthUtils.check(user, TrustLevel.ADMINISTRATOR)) return;

        CreateTrashCanPayload payload = ctx.bodyAsClass(CreateTrashCanPayload.class);

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
                        .bind("lat", payload.lat())
                        .bind("lon", payload.lon())
                        .bind("name", payload.name())
                        .bind("description", payload.description())
                        .bind("author", payload.author())
                        .bind("broken", payload.broken())
                        .bind("ashtray", payload.ashtray())
                        .bind("windblown", payload.windblown())
                        .bind("flooded", payload.flooded())
                        .bind("overwhelmed", payload.overwhelmed())
                        .bind("poopbag", payload.poopbag())
                        .bind("art", payload.art())
                        .execute()
        );
    }

    record CreateTrashCanPayload(
            double lat, double lon, String name, String description, long author,
            boolean broken,
            boolean ashtray, boolean windblown, boolean flooded, boolean overwhelmed, boolean poopbag, boolean art
    ) {}
}
