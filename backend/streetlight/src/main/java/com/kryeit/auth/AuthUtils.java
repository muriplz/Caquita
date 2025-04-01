package com.kryeit.auth;

import com.kryeit.Database;
import com.kryeit.Utils;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Map;

public class AuthUtils {

    public static long getUser(Context ctx) {
        String token = ctx.cookie("auth");
        if (token == null) {
            throw new UnauthorizedResponse("Auth token is missing");
        }

        long user = Jwt.validateToken(token);
        if (user == -1) {
            throw new UnauthorizedResponse("Invalid auth token");
        }
        return user;
    }

    public static boolean check(long user, TrustLevel trust) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT trust FROM users WHERE id = :id")
                        .bind("id", user)
                        .mapTo(TrustLevel.class)
                        .findOne()
                        .orElse(TrustLevel.DEFAULT)
                        .ordinal() >= trust.ordinal()
        );
    }

    public static void getUsername(Context ctx) {
        getUser(ctx);

        long user = Utils.getIdFromParam(ctx);

        String username = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT username FROM users WHERE id = :id")
                        .bind("id", user)
                        .mapTo(String.class)
                        .one()
        );

        ctx.json(Map.of(
                "username", username
        ));
    }
}
