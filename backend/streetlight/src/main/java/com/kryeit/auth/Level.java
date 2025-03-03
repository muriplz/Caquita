package com.kryeit.auth;

import com.kryeit.Database;
import com.kryeit.Utils;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Map;

public class Level {

    public User user;
    public int experience;

    public static int LEVEL_1_REQUIRED_EXP = 100;

    public Level(User user) {
        this.user = user;
        this.experience = user.experience();
    }



    public void updateTrust() {
        switch (getLevel()) {
            case 20 -> user.changeTrust(TrustLevel.TRUSTED);
            case 25 -> user.changeTrust(TrustLevel.CONTRIBUTOR);
            case 30 -> user.changeTrust(TrustLevel.MODERATOR);
        }
    }

    public int getNextLevelRequiredExp() {
        return (int)(LEVEL_1_REQUIRED_EXP * Math.pow(1.1, getLevel()));
    }

    public int getCurrentLevelExp() {
        int totalExpForCurrentLevel = 0;
        int level = getLevel();

        for (int i = 0; i < level; i++) {
            totalExpForCurrentLevel += (int)(LEVEL_1_REQUIRED_EXP * Math.pow(1.1, i));
        }

        return this.experience - totalExpForCurrentLevel;
    }

    public int getCurrentLevelTotalExp() {
        int currentLevel = getLevel();
        if (currentLevel == 0) {
            return LEVEL_1_REQUIRED_EXP;
        }
        return (int) (LEVEL_1_REQUIRED_EXP * Math.pow(1.1, currentLevel - 1));
    }

    public int getLevel() {
        int exp = this.experience;
        int level;

        if (exp < LEVEL_1_REQUIRED_EXP) {
            return 0;
        }

        level = 1;
        int expRequired = LEVEL_1_REQUIRED_EXP;

        while (exp >= expRequired) {
            expRequired = (int)(LEVEL_1_REQUIRED_EXP * Math.pow(1.1, level));

            level++;
            exp -= expRequired;
        }

        return level;
    }


    public static void getLevel(Context ctx) {
        long id = Utils.getIdFromPath(ctx);

        User user = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE id = :id")
                        .bind("id", id)
                        .mapTo(User.class)
                        .first()
        );

        Level level = new Level(user);

        ctx.json(Map.of(
                "level", level.getLevel(),
                "experience", level.experience,
                "level-progress", level.getCurrentLevelExp(),
                "level-total", level.getCurrentLevelTotalExp(),
                "next-level-total", level.getNextLevelRequiredExp()
        ));
    }


    public static void modifyLevel(Context ctx) {
        long id = AuthUtils.getUser(ctx);
        JSONObject body = new JSONObject(ctx.body());

        int amount = body.getInt("amount");

        User user = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE id = :id")
                        .bind("id", id)
                        .mapTo(User.class)
                        .first()
        );

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE users SET experience = experience + :amount WHERE id = :id")
                        .bind("amount", amount)
                        .bind("id", id)
                        .execute()
        );

        new Level(user).updateTrust();
    }
}
