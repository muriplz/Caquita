package com.kryeit.auth;

import com.kryeit.Database;
import com.kryeit.auth.inventory.InventoryApi;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class LoginApi {

    public static boolean inputRestrictions(Context ctx, String username, String password) {
        if (username.length() < 3 || username.length() > 16) {
            ctx.status(400).result("Username must be between 3 and 16 characters.");
            return false;
        }

        if (password.length() < 6 || password.length() > 32) {
            ctx.status(400).result("Password must be between 6 and 32 characters.");
            return false;
        }

        return true;
    }

    /**
     * HTTP POST Request to /api/v1/auth/login
     * Logs in a user.
     *
     * JSON Body Parameters:
     * - username: The username of the user.
     * - password: The password of the user.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void login(Context ctx) {
        JSONObject body = new JSONObject(ctx.body());

        String username = body.getString("username");
        String password = body.getString("password");

        if (!inputRestrictions(ctx, username, password)) {
            return;
        }

        User user = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE LOWER(username) = LOWER(:username)")
                        .bind("username", username)
                        .mapTo(User.class)
                        .findOne()
                        .orElse(null)
        );

        if (user != null && BCrypt.checkpw(password, user.password())) {
            String token = Jwt.generateToken(user.id());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("id", String.valueOf(user.id()));
            response.put("username", user.username());
            response.put("creation", user.creation().toString());
            response.put("trust", user.trust().toString());
            response.put("experience", String.valueOf(user.experience()));
            response.put("beans", String.valueOf(user.beans()));
            ctx.status(200).json(response);
        } else {
            ctx.status(401).result("Invalid username or password.");
        }
    }

    /**
     * HTTP POST Request to /api/v1/auth/register
     * Registers a new user.
     *
     * JSON Body Parameters:
     * - username: The username of the user.
     * - password: The password of the user.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void register(Context ctx) {
        JSONObject body = new JSONObject(ctx.body());
        String username = body.getString("username");
        String password = body.getString("password");

        if (!inputRestrictions(ctx, username, password)) {
            return;
        }

        if (Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(:username)")
                        .bind("username", username)
                        .mapTo(Integer.class)
                        .one()) > 0) {
            ctx.status(400).result("Username is already taken.");
            return;
        }

        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            long id = Database.getJdbi().withHandle(handle ->
                    handle.createUpdate("INSERT INTO users (username, password, creation, trust, experience, beans) VALUES (:username, :password, NOW(), 'DEFAULT', 0, 50)")
                            .bind("username", username)
                            .bind("password", hashedPassword)
                            .executeAndReturnGeneratedKeys("id")
                            .mapTo(Long.class)
                            .one()
            );

            InventoryApi.initInventory(id);

            ctx.status(201).result("User registered successfully.");

        } catch (Exception e) {
            ctx.status(500).result("Registration failed due to internal error.");
        }
    }

    /**
     * HTTP GET Request to /api/v1/auth/validate
     * Validates the user's token and returns the user information.
     *
     * @param ctx the Javalin HTTP context
     */
    public static void validate(Context ctx) {
        String token = ctx.cookie("auth");

        if (token == null) {
            throw new UnauthorizedResponse();
        }

        long id = Jwt.validateToken(token);

        if (id == -1) {
            throw new UnauthorizedResponse();
        }

        Map<String, Object> data = Database.getJdbi().withHandle(handle -> handle.createQuery("""
            SELECT id, username, creation, trust, experience, beans
            FROM users
            WHERE id = :id
            """)
                .bind("id", id)
                .mapToMap()
                .one());

        ctx.json(Map.of(
                "id", data.get("id"),
                "username", data.get("username"),
                "creation", data.get("creation"),
                "trust", data.get("trust"),
                "experience", data.get("experience"),
                "beans", data.get("beans")
        ));
    }
}