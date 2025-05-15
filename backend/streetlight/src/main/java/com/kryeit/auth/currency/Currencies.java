package com.kryeit.auth.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.Database;
import com.kryeit.auth.Level;

public record Currencies(long id, int experience, int beans) {

    public enum CurrencyType {
        LEVEL,
        BEANS,
    }

    public static void create(long userId) {

        Database.getJdbi().withHandle(handle -> {
            handle.createUpdate("""
                            INSERT INTO currencies (id, experience, beans, rolls)
                            VALUES (:id, :experience, :beans)
                            """)
                    .bind("id", userId)
                    .bind("experience", 0)
                    .bind("beans", 0)
                    .execute();
            return true;
        });
    }

}