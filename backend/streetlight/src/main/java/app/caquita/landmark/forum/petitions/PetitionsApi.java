package app.caquita.landmark.forum.petitions;

import com.fasterxml.jackson.databind.JsonNode;
import app.caquita.Database;
import app.caquita.auth.AuthUtils;
import app.caquita.auth.User;
import app.caquita.auth.avatar.UnlockedAvatar;
import app.caquita.landmark.LandmarkType;
import app.caquita.landmark.trash_can.TrashCanApi;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetitionsApi {

    public static void getPetitions(Context ctx) {
        AuthUtils.getUser(ctx);

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
                SELECT id, description, user_id, type, ST_Y(position) as lat, ST_X(position) as lon, info, status, creation, edition
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

        // Validate landmark info matches the expected structure for the type TODO
        //validateLandmarkInfoStructure(ctx, payload.type(), payload.info);

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("""
                INSERT INTO petitions (description, user_id, type, position, info, status)
                VALUES (:description, :userId, :type, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), cast(:info as jsonb), 'PENDING')
                """)
                    .bind("description", payload.description())
                    .bind("userId", user)
                    .bind("type", payload.type().name())
                    .bind("lat", payload.lat())
                    .bind("lon", payload.lon())
                    .bind("info", payload.info())
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
            if (!key.equals("name")) {
                JsonNode valueNode = entry.getValue();
                String value = valueNode.isBoolean() ? String.valueOf(valueNode.asBoolean()) : valueNode.asText();
                //if (!LandmarkFeatureDefinitions.isValidFeature(type, key, value)) {
                //    ctx.status(400).result("Invalid feature for landmark type.");
                //}
            }
        });
    }

    public static void getPetition(Context ctx) {
        AuthUtils.getUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                        SELECT id, description, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                               info, status, creation, edition
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
                        SELECT id, description, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                               info, status, creation, edition
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

        if (petition.userId() != user && !AuthUtils.check(user, User.Trust.ADMINISTRATOR)) {
            ctx.status(403).result("You are not the owner of this petition.");
            return;
        }

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("""
                    UPDATE petitions
                    SET position = ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                        info = cast(:info as jsonb),
                        edition = NOW()
                    WHERE id = :id
                    """)
                    .bind("lat", payload.lat())
                    .bind("lon", payload.lon())
                    .bind("info", payload.info())
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
                    SELECT id, description, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                           info, status, creation, edition, image
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

        if (petition.userId() != user && !AuthUtils.check(user, User.Trust.ADMINISTRATOR)) {
            ctx.status(403).result("You are not the owner of this petition.");
            return;
        }

        PetitionImageApi.deleteImage(id);

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("DELETE FROM petitions WHERE id = :id")
                    .bind("id", id)
                    .execute();
        });

        ctx.status(204);
    }

    public static void updateStatus(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));
        String bodyString = ctx.body();
        JSONObject body = new JSONObject(bodyString);
        String status = body.getString("status");

        Petition petition = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                    SELECT id, description, user_id, type, ST_Y(position) as lat, ST_X(position) as lon,
                           info, status, creation, edition, image
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

        if (!AuthUtils.check(user, User.Trust.ADMINISTRATOR)) {
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
                String jsonString = petition.info().toString();
                JSONObject info = new JSONObject(jsonString);
                String name = info.getString("name");

                JSONObject featuresOnly = new JSONObject(jsonString);
                featuresOnly.remove("name");

                switch (petition.type()) {
                    case TRASH_CAN -> TrashCanApi.create(
                            petition.lat(),
                            petition.lon(),
                            name,
                            petition.description(),
                            petition.userId(),
                            featuresOnly
                            );
                }

                UnlockedAvatar.grant("bottles", petition.userId());

                //PetitionImageApi.acceptImage(petition.id(), landmarkId);

                ctx.status(200).json(Map.of(
                        "message", "Petition accepted and landmark created"
                  //      "landmarkId", landmarkId
                ));
                return;
            } catch (Exception e) {
                ctx.status(500).result("Error creating landmark: " + e.getMessage());
                return;
            }
        }

        ctx.status(204);
    }

    public static void sendMessage(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        long petitionId = Long.parseLong(ctx.pathParam("id"));

        String message = new JSONObject(ctx.body()).getString("message");

        long nonSpaceCount = message.chars().filter(c -> !Character.isWhitespace(c)).count();

        if (nonSpaceCount < 6) {
            ctx.status(400).result("Message too short.");
            return;
        }

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("""
                INSERT INTO petition_messages (petition_id, user_id, content)
                VALUES (:petitionId, :userId, :content)
                """)
                    .bind("petitionId", petitionId)
                    .bind("userId", user)
                    .bind("content", message)
                    .execute();
        });
        ctx.status(201);
    }

    public static void sendReply(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        long messageId = Long.parseLong(ctx.pathParam("id"));

        String reply = new JSONObject(ctx.body()).getString("message");

        long nonSpaceCount = reply.chars().filter(c -> !Character.isWhitespace(c)).count();

        if (nonSpaceCount < 6) {
            ctx.status(400).result("Reply too short.");
            return;
        }

        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("""
                INSERT INTO petition_replies (message_id, user_id, content)
                VALUES (:messageId, :userId, :content)
                """)
                    .bind("messageId", messageId)
                    .bind("userId", user)
                    .bind("content", reply)
                    .execute();
        });

        ctx.status(201);
    }

    public record UpdatePetitionPayload(double lat, double lon, HashMap<String, Boolean> info) {}

    public record CreatePetitionPayload(String description, LandmarkType type, double lat, double lon, HashMap<String, Boolean> info) {}

    public static class MessagesApi {

        public static void getMessages(Context ctx) {
            long user = AuthUtils.getUser(ctx);
            long petitionId = Long.parseLong(ctx.pathParam("id"));

            List<Map<String, Object>> messages = Database.getJdbi().withHandle(handle ->
                    handle.createQuery("""
                    SELECT pm.id, pm.petition_id, pm.user_id, pm.content, pm.creation, u.username
                    FROM petition_messages pm
                    JOIN users u ON pm.user_id = u.id
                    WHERE pm.petition_id = :petitionId
                    ORDER BY pm.creation DESC
                    """)
                            .bind("petitionId", petitionId)
                            .mapToMap()
                            .list()
            );

            ctx.json(messages);
        }

        public static void getReplies(Context ctx) {
            AuthUtils.getUser(ctx);
            long messageId = Long.parseLong(ctx.pathParam("id"));

            List<Map<String, Object>> replies = Database.getJdbi().withHandle(handle ->
                    handle.createQuery("""
                    SELECT pr.id, pr.message_id, pr.user_id, pr.content, pr.creation, u.username
                    FROM petition_replies pr
                    JOIN users u ON pr.user_id = u.id
                    WHERE pr.message_id = :messageId
                    ORDER BY pr.creation DESC
                    """)
                            .bind("messageId", messageId)
                            .mapToMap()
                            .list()
            );

            ctx.json(replies);
        }
    }
}

