package com.kryeit.landmark.forum.petitions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.kryeit.auth.TrustLevel;
import com.kryeit.auth.User;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PetitionsApi {
    public static void getPetitions(Context ctx) {
        long user = AuthUtils.getUser(ctx);

        GetPetitionsPayload payload = ctx.bodyAsClass(GetPetitionsPayload.class);

        List<Petition> petitions = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM petitions WHERE status = 'PENDING' ORDER BY created_at DESC LIMIT 10 OFFSET :offset")
                        .bind("offset", payload.page() * 10)
                        .mapTo(Petition.class)
                        .list()
        );

        ctx.json(petitions);
    }

    public static void createPetition(Context ctx) {
        long user = AuthUtils.getUser(ctx);

        CreatePetitionPayload payload = ctx.bodyAsClass(CreatePetitionPayload.class);
        //public record Petition(
        //        long id, long userId, double lat, double lon, ObjectNode landmarkInfo, boolean accepted,
        //        Timestamp creation, Timestamp edition) {
        //}

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("INSERT INTO petitions (user_id, lat, lon, landmark_info) VALUES (:userId, :lat, :lon, :landmarkInfo)")
                    .bind("userId", user)
                    .bind("lat", payload.lat())
                    .bind("lon", payload.lon())
                    .bind("landmarkInfo", payload.landmarkInfo())
                    .execute();
        });

        ctx.status(201);
    }

    public static void getPetition(Context ctx) {
        long user = AuthUtils.getUser(ctx);

        long id = Long.parseLong(ctx.pathParam("id"));

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM petitions WHERE id = :id")
                        .bind("id", id)
                        .mapTo(Petition.class)
                        .findOne()
                        .orElse(null)
        );

        if (petition == null) {
            ctx.status(404).result("Petition not found.");
            return;
        }

        ctx.json(petition);
    }

    public static void updatePetition(Context ctx) {

        long user = AuthUtils.getUser(ctx);

        long id = Long.parseLong(ctx.pathParam("id"));

        UpdatePetitionPayload payload = ctx.bodyAsClass(UpdatePetitionPayload.class);

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM petitions WHERE id = :id")
                        .bind("id", id)
                        .mapTo(Petition.class)
                        .findOne()
                        .orElse(null)
        );

        if (petition == null) {
            ctx.status(404).result("Petition not found.");
            return;
        }

        if (petition.userId() != user || AuthUtils.check(user, TrustLevel.ADMINISTRATOR)) {
            ctx.status(403).result("You are not the owner of this petition.");
            return;
        }

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE petitions SET lat = :lat, lon = :lon, landmark_info = :landmarkInfo WHERE id = :id")
                    .bind("lat", payload.lat())
                    .bind("lon", payload.lon())
                    .bind("landmarkInfo", payload.landmarkInfo())
                    .bind("id", id)
                    .execute();
        });

        ctx.status(204);
    }

    public static void deletePetition(Context ctx) {
        long user = AuthUtils.getUser(ctx);

        long id = Long.parseLong(ctx.pathParam("id"));

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM petitions WHERE id = :id")
                        .bind("id", id)
                        .mapTo(Petition.class)
                        .findOne()
                        .orElse(null)
        );

        if (petition == null) {
            ctx.status(404).result("Petition not found.");
            return;
        }

        if (petition.userId() != user || AuthUtils.check(user, TrustLevel.ADMINISTRATOR)) {
            ctx.status(403).result("You are not the owner of this petition.");
            return;
        }

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("DELETE FROM petitions WHERE id = :id")
                    .bind("id", id)
                    .execute();
        });

        ctx.status(204);
    }

    public static void acceptPetition(Context ctx) {
    }


    public record UpdatePetitionPayload(double lat, double lon, ObjectNode landmarkInfo) {

    }

    public record CreatePetitionPayload(double lat, double lon, ObjectNode landmarkInfo) {
    }

    public record GetPetitionsPayload(int page) {
    }


}
