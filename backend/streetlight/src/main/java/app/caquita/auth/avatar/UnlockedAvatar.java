package app.caquita.auth.avatar;

import app.caquita.storage.Database;
import app.caquita.auth.AuthUtils;
import io.javalin.http.Context;
import org.json.JSONObject;

import java.sql.Timestamp;

public record UnlockedAvatar(long id, long userId, String avatar, Timestamp timestamp) {

    public static void init(long user) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO unlocked_avatars (user_id, avatar)
                        VALUES (:userId, :avatar)
                        """)
                        .bind("userId", user)
                        .bind("avatar", "trash_can")
                        .execute()
        );
    }

    public static void getUnlockedAvatars(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        var avatars = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM unlocked_avatars WHERE user_id = :userId")
                        .bind("userId", user)
                        .mapTo(UnlockedAvatar.class)
                        .list()
        );

        ctx.json(avatars);
    }

    public static void changeAvatar(Context ctx) {
        long user = AuthUtils.getUser(ctx);
        String avatar = new JSONObject(ctx.body()).optString("avatar");

        if (avatar == null) {
            ctx.status(400).json("Avatar is missing");
            return;
        }

        boolean isUnlocked = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM unlocked_avatars WHERE user_id = :userId AND avatar = :avatar")
                        .bind("userId", user)
                        .bind("avatar", avatar)
                        .mapTo(Integer.class)
                        .one() > 0
        );

        if (!isUnlocked) {
            ctx.status(400).json("Avatar is not unlocked");
            return;
        }

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE users SET avatar = :avatar WHERE id = :userId")
                        .bind("avatar", avatar)
                        .bind("userId", user)
                        .execute()
        );

        ctx.status(201);
    }

    public static void grant(String avatar, long user) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO unlocked_avatars (user_id, avatar)
                        VALUES (:userId, :avatar)
                        """)
                        .bind("userId", user)
                        .bind("avatar", avatar)
                        .execute()
        );
    }
}
