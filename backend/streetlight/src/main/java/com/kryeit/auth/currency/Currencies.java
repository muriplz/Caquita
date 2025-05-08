package com.kryeit.auth.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.auth.Level;

public record Currencies(long id, ObjectNode level, int beans, int rolls) {

    public enum CurrencyType {
        LEVEL,
        BEANS,
        ROLLS
    }

    public static void create(long userId) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode levelNode = mapper.createObjectNode();
        levelNode.put("experience", 0);
        levelNode.put("level", 1);
        levelNode.put("progress", 0);
        levelNode.put("total", Level.LEVEL_REQUIRED_EXP[1]);

        Database.getJdbi().withHandle(handle -> {
            handle.createUpdate("INSERT INTO currencies (id, level, beans, rolls) VALUES (:id, cast(:level as jsonb), :beans, :rolls)")
                    .bind("id", userId)
                    .bind("level", levelNode.toString())
                    .bind("beans", 0)
                    .bind("rolls", 2)
                    .execute();
            return true;
        });
    }

}