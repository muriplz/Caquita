package com.kryeit.auth;

import com.kryeit.Database;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendshipApi {

    public static void getFriends(Context ctx) {
        long userId = AuthUtils.getUser(ctx);

        List<Map<String, Object>> friends = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                SELECT u.id, u.username, u.creation, u.connection, c.level, f.status, f.creation as friendship_since
                FROM users u
                JOIN friendships f ON u.id = f.friend_id
                JOIN currencies c ON u.id = c.id
                WHERE f.user_id = :userId AND f.status = 'ACCEPTED'
                UNION
                SELECT u.id, u.username, u.creation, u.connection, c.level, f.status, f.creation as friendship_since
                FROM users u
                JOIN friendships f ON u.id = f.user_id
                JOIN currencies c ON u.id = c.id
                WHERE f.friend_id = :userId AND f.status = 'ACCEPTED'
                """)
                        .bind("userId", userId)
                        .mapToMap()
                        .list()
        );

        ctx.json(friends);
    }

    public static void getPendingRequests(Context ctx) {
        long userId = AuthUtils.getUser(ctx);

        List<Map<String, Object>> requests = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                SELECT u.id, u.username, u.creation, f.creation as request_date
                FROM users u
                JOIN friendships f ON u.id = f.user_id
                WHERE f.friend_id = :userId AND f.status = 'PENDING'
                """)
                        .bind("userId", userId)
                        .mapToMap()
                        .list()
        );

        ctx.json(requests);
    }

    public static void getSentRequests(Context ctx) {
        long userId = AuthUtils.getUser(ctx);

        List<Map<String, Object>> sentRequests = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                SELECT u.id, u.username, u.creation, f.creation as request_date
                FROM users u
                JOIN friendships f ON u.id = f.friend_id
                WHERE f.user_id = :userId AND f.status = 'PENDING'
                """)
                        .bind("userId", userId)
                        .mapToMap()
                        .list()
        );

        ctx.json(sentRequests);
    }

    public static void sendRequest(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        JSONObject body = new JSONObject(ctx.body());
        System.out.println("Body: " + body);
        String friend = body.getString("username");

        // Check if user exists and check if trying to add self
        long friendId = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT id FROM users WHERE username = :username")
                        .bind("username", friend)
                        .mapTo(long.class)
                        .findOne()
                        .orElseThrow(() -> new NotFoundResponse("User not found"))
        );

        if (userId == friendId) {
            ctx.status(400).result("Cannot send a friend request to yourself");
            return;
        }

        // Check if user exists
        boolean userExists = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM users WHERE id = :userId")
                        .bind("userId", friendId)
                        .mapTo(long.class)
                        .one() > 0
        );

        if (!userExists) {
            throw new NotFoundResponse("User not found");
        }

        // Check if relationship exists already
        String existingStatus = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                SELECT status FROM friendships
                WHERE (user_id = :userId AND friend_id = :friendId)
                OR (user_id = :friendId AND friend_id = :userId)
                """)
                        .bind("userId", userId)
                        .bind("friendId", friendId)
                        .mapTo(String.class)
                        .findOne()
                        .orElse(null)
        );

        if (existingStatus != null) {
            ctx.status(400).result("Friendship relationship already exists with status: " + existingStatus);
            return;
        }

        // Create new friendship request
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                INSERT INTO friendships (user_id, friend_id, status, creation, edition)
                VALUES (:userId, :friendId, 'PENDING', NOW(), NOW())
                """)
                        .bind("userId", userId)
                        .bind("friendId", friendId)
                        .execute()
        );

        Map<String, Object> user = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                SELECT u.username, c.level
                FROM users u
                JOIN currencies c ON u.id = c.id
                WHERE u.id = :friendId
                """)
                        .bind("friendId", friendId)
                        .mapToMap()
                        .one()
        );

        ctx.status(201).json(Map.of(
                "username", user.get("username"),
                "level", user.get("level")
        ));
    }

    public static void respondToRequest(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        JSONObject body = new JSONObject(ctx.body());
        long requesterId = body.getLong("requesterId");
        String action = body.getString("action").toUpperCase();

        if (!action.equals("ACCEPTED") && !action.equals("REJECTED")) {
            ctx.status(400).result("Action must be 'ACCEPTED' or 'REJECTED'");
            return;
        }

        // Update friendship status
        int updated = Database.getJdbi().withHandle(handle ->
                handle.createUpdate("""
                UPDATE friendships
                SET status = :status, edition = NOW()
                WHERE user_id = :requesterId AND friend_id = :userId AND status = 'PENDING'
                """)
                        .bind("status", action)
                        .bind("requesterId", requesterId)
                        .bind("userId", userId)
                        .execute()
        );

        if (updated == 0) {
            ctx.status(404).result("No pending request found from this user");
            return;
        }

        ctx.status(200).result("Friend request " + action.toLowerCase());
    }

    public static void removeFriend(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        long friendId = Long.parseLong(ctx.pathParam("friendId"));

        int deleted = Database.getJdbi().withHandle(handle ->
                handle.createUpdate("""
                DELETE FROM friendships
                WHERE (user_id = :userId AND friend_id = :friendId)
                OR (user_id = :friendId AND friend_id = :userId)
                """)
                        .bind("userId", userId)
                        .bind("friendId", friendId)
                        .execute()
        );

        if (deleted == 0) {
            ctx.status(404).result("Friendship not found");
            return;
        }

        ctx.status(200).result("Friend removed");
    }

    public static void blockUser(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        JSONObject body = new JSONObject(ctx.body());
        long blockId = body.getLong("blockId");

        if (userId == blockId) {
            ctx.status(400).result("Cannot block yourself");
            return;
        }

        // Delete any existing relationship first
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                DELETE FROM friendships
                WHERE (user_id = :userId AND friend_id = :blockId)
                OR (user_id = :blockId AND friend_id = :userId)
                """)
                        .bind("userId", userId)
                        .bind("blockId", blockId)
                        .execute()
        );

        // Create block relationship
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                INSERT INTO friendships (user_id, friend_id, status, creation, edition)
                VALUES (:userId, :blockId, 'BLOCKED', NOW(), NOW())
                """)
                        .bind("userId", userId)
                        .bind("blockId", blockId)
                        .execute()
        );

        ctx.status(200).result("User blocked");
    }

    public static void getAvatars(Context ctx) {
        long user = AuthUtils.getUser(ctx);

        Map<String, String> avatars = Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                SELECT f.friend_id, u.avatar FROM friendships f
                JOIN users u ON f.friend_id = u.id
                WHERE f.user_id = :userId AND f.status = 'ACCEPTED'
                UNION
                SELECT f.user_id as friend_id, u.avatar FROM friendships f
                JOIN users u ON f.user_id = u.id
                WHERE f.friend_id = :userId AND f.status = 'ACCEPTED'
                """)
                        .bind("userId", user)
                        .reduceRows(new HashMap<>(), (map, row) -> {
                            map.put(String.valueOf(row.getColumn("friend_id", Long.class)),
                                    row.getColumn("avatar", String.class));
                            return map;
                        })
        );

        ctx.json(avatars);
    }
}