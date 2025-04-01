package com.kryeit.landmark.forum.petitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.kryeit.auth.TrustLevel;
import com.kryeit.landmark.LandmarkApi;
import com.kryeit.landmark.LandmarkFeatureDefinitions;
import com.kryeit.landmark.LandmarkType;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class PetitionsApi {

    public static void getPetitions(Context ctx) {
        long user = AuthUtils.getUser(ctx);

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        String orderBy = ctx.queryParam("orderBy");
        String status = ctx.queryParam("status");

        if (orderBy == null || (!orderBy.equals("ASC") && !orderBy.equals("DESC"))) {
            orderBy = "DESC";
        }

        if (status == null || (!status.equals("PENDING") && !status.equals("ACCEPTED") && !status.equals("REJECTED"))) {
            status = "PENDING";
        }

        String query = """
                SELECT id, user_id, type, ST_Y(position) as lat, ST_X(position) as lon, landmark_info, status, creation, edition
                FROM petitions
                WHERE status = :status
                ORDER BY creation %s
                LIMIT 10 OFFSET :offset
                """.formatted(orderBy);

        String finalStatus = status;
        List<Petition> petitions = Database.getJdbi().withHandle(handle ->
                handle.createQuery(query)
                        .bind("offset", page * 10)
                        .bind("status", finalStatus)
                        .mapTo(Petition.class)
                        .list()
        );

        int totalPetitions = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM petitions WHERE status = :status")
                        .bind("status", finalStatus)
                        .mapTo(Integer.class)
                        .one()
        );

        ctx.json(
                Map.of(
                        "petitions", petitions,
                        "maxPages", Math.ceil(totalPetitions / 10.0)
                )
        );
    }

    public static void createPetition(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        CreatePetitionPayload payload = ctx.bodyAsClass(CreatePetitionPayload.class);

        System.out.println(payload.landmarkInfo());

        // Validate landmark info matches the expected structure for the type
        validateLandmarkInfoStructure(ctx, payload.type(), payload.landmarkInfo());

        String landmarkInfoJson = payload.landmarkInfo().toString();

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("""
                INSERT INTO petitions (user_id, type, position, landmark_info, status)
                VALUES (:userId, :type, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), cast(:landmarkInfo as jsonb), 'PENDING')
                """)
                    .bind("userId", user)
                    .bind("type", payload.type().name())
                    .bind("lat", payload.lat())
                    .bind("lon", payload.lon())
                    .bind("landmarkInfo", landmarkInfoJson)
                    .execute();
        });

        ctx.status(201);
    }


    private static void validateLandmarkInfoStructure(Context ctx, LandmarkType type, JsonNode landmarkInfoJson) {
        if (!landmarkInfoJson.has("name") || landmarkInfoJson.get("name").isNull()) {
            throw new BadRequestResponse("Landmark info must include a name");
        }

        landmarkInfoJson.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if (!key.equals("name") && !LandmarkFeatureDefinitions.isValidFeature(type, key, entry.getValue().asText())) {
                ctx.status(400).result("Invalid feature for landmark type.");
            }
        });
    }

    public static void getPetition(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                        SELECT id, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                               landmark_info, status, creation, edition
                        FROM petitions
                        WHERE id = :id
                        """)
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
                handle.createQuery("""
                        SELECT id, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                               landmark_info, status, creation, edition
                        FROM petitions
                        WHERE id = :id
                        """)
                        .bind("id", id)
                        .mapTo(Petition.class)
                        .findOne()
                        .orElse(null)
        );

        if (petition == null) {
            ctx.status(404).result("Petition not found.");
            return;
        }

        if (petition.userId() != user && !AuthUtils.check(user, TrustLevel.ADMINISTRATOR)) {
            ctx.status(403).result("You are not the owner of this petition.");
            return;
        }

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("""
                    UPDATE petitions
                    SET position = ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                        landmark_info = cast(:landmarkInfo as jsonb),
                        edition = NOW()
                    WHERE id = :id
                    """)
                    .bind("lat", payload.lat())
                    .bind("lon", payload.lon())
                    .bind("landmarkInfo", payload.landmarkInfo().toString())
                    .bind("id", id)
                    .execute();
        });


        ctx.status(204);
    }

    public static void deletePetition(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                        SELECT id, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                               landmark_info, status, creation, edition
                        FROM petitions
                        WHERE id = :id
                        """)
                        .bind("id", id)
                        .mapTo(Petition.class)
                        .findOne()
                        .orElse(null)
        );

        if (petition == null) {
            ctx.status(404).result("Petition not found.");
            return;
        }

        if (petition.userId() != user && !AuthUtils.check(user, TrustLevel.ADMINISTRATOR)) {
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
        // Implementation needed
    }

    public static void updateStatus(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));
        String bodyString = ctx.body();
        JSONObject body = new JSONObject(bodyString);
        String status = body.getString("status");

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                    SELECT id, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                           landmark_info, status, creation, edition
                    FROM petitions
                    WHERE id = :id
                    """)
                        .bind("id", id)
                        .mapTo(Petition.class)
                        .findOne()
                        .orElse(null)
        );

        if (petition == null) {
            ctx.status(404).result("Petition not found.");
            return;
        }

        if (!AuthUtils.check(user, TrustLevel.ADMINISTRATOR)) {
            ctx.status(403).result("You are not an administrator.");
            return;
        }

        if (!status.equals("ACCEPTED") && !status.equals("REJECTED")) {
            ctx.status(400).result("Invalid status.");
            return;
        }

        boolean updated = Database.getJdbi().withHandle(handle -> {
            int rowsUpdated = handle.createUpdate("""
                UPDATE petitions
                SET status = :status, edition = NOW()
                WHERE id = :id
                """)
                    .bind("status", status)
                    .bind("id", id)
                    .execute();
            return rowsUpdated > 0;
        });

        if (updated && status.equals("ACCEPTED")) {
            try {
                String jsonString = petition.landmarkInfo().toString();
                JSONObject landmarkInfo = new JSONObject(jsonString);
                String name = landmarkInfo.getString("name");

                JSONObject featuresOnly = new JSONObject(jsonString);
                featuresOnly.remove("name");

                long landmarkId = LandmarkApi.create(
                        name,
                        petition.lat(),
                        petition.lon(),
                        petition.type(),
                        featuresOnly.toString()
                );

                ctx.status(200).json(Map.of(
                        "message", "Petition accepted and landmark created",
                        "landmarkId", landmarkId
                ));
                return;
            } catch (Exception e) {
                ctx.status(500).result("Error creating landmark: " + e.getMessage());
                return;
            }
        }

        ctx.status(204);
    }

    public record UpdatePetitionPayload(double lat, double lon, ObjectNode landmarkInfo) {}

    public record CreatePetitionPayload(LandmarkType type, double lat, double lon, ObjectNode landmarkInfo) {}

    public static class MessagesApi {

        public static void getMessages(Context ctx) {
            long user = AuthUtils.getUser(ctx);
            long petitionId = Long.parseLong(ctx.pathParam("id"));

            List<PetitionMessage> messages = Database.getJdbi().withHandle(handle ->
                    handle.createQuery("""
                            SELECT id, petition_id, user_id, content, creation
                            FROM petition_messages
                            WHERE petition_id = :petitionId
                            ORDER BY creation DESC
                            """)
                            .bind("petitionId", petitionId)
                            .mapTo(PetitionMessage.class)
                            .list()
            );

            ctx.json(messages);
        }
    }
}