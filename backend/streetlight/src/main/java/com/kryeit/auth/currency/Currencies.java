package com.kryeit.auth.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.auth.Level;
import com.kryeit.sync.Syncable;

public record Currencies(long id, Level level, int beans, int rolls) implements Syncable {

    public enum CurrencyType {
        LEVEL,
        BEANS,
        ROLLS
    }

    public static void create(long userId) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode levelNode = mapper.createObjectNode();
        levelNode.put("experience", 0);
        levelNode.put("level", 0);
        levelNode.put("level-progress", 0);
        levelNode.put("level-total", Level.LEVEL_1_REQUIRED_EXP);
        levelNode.put("next-level-total", Level.LEVEL_1_REQUIRED_EXP);

        Database.getJdbi().withHandle(handle -> {
            handle.createUpdate("INSERT INTO currencies (id, level, beans, rolls) VALUES (:id, :level, :beans, :rolls)")
                    .bind("id", userId)
                    .bind("level", levelNode)
                    .bind("beans", 0)
                    .bind("rolls", 0)
                    .execute();
            return getCurrencies(userId);
        });
    }

    public static Currencies getCurrencies(long userId) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM currencies WHERE id = :userId")
                        .bind("userId", userId)
                        .mapTo(Currencies.class)
                        .findFirst()
                        .orElseGet(() -> {
                            create(userId);
                            return getCurrencies(userId);
                        })
        );
    }

    @Override
    public String getEntityType() {
        return "currencies";
    }

    @Override
    public Long getUserId() {
        return id;
    }
}