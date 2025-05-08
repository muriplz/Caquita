package com.kryeit.auth;

import com.kryeit.Database;

public class LevelApi {
    public static void modifyLevel(long user, int amount) {
        int newExperience = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT (level->>'experience')::int FROM currencies WHERE id = :user")
                        .bind("user", user)
                        .mapTo(int.class)
                        .one()
        ) + amount;

        Level newLevel = Level.fromExperience(newExperience);

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE currencies SET level = cast(:level as jsonb) WHERE id = :user")
                        .bind("level", newLevel.toJson().toString())
                        .bind("user", user)
                        .execute()
        );

    }
}
