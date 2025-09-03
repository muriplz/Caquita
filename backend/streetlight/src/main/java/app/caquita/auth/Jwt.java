package app.caquita.auth;

import app.caquita.Config;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.javalin.http.UnauthorizedResponse;

import java.time.Duration;
import java.util.Date;

public class Jwt {

    public static final Algorithm algorithm = Algorithm.HMAC256(Config.JWT_SECRET);
    private static final long EXPIRATION = Duration.ofDays(30).toMillis();

    public static String generateToken(long id) {
        JWTCreator.Builder token = JWT.create()
                .withClaim("id", id)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION));

        return token.sign(algorithm);
    }

    public static long validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("id").asLong();
        } catch (JWTVerificationException e) {
            throw new UnauthorizedResponse("Invalid token");
        }
    }

}