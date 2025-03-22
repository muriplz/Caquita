package com.kryeit.auth;

import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

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
}
