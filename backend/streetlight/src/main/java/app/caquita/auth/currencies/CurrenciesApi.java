package app.caquita.auth.currencies;

import app.caquita.Database;

public class CurrenciesApi {

    public static void create(long userId) {

        Database.getJdbi().withHandle(handle -> {
            handle.createUpdate("""
                            INSERT INTO currencies (id, experience, beans)
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
