package com.kryeit.auth.currency;

import com.kryeit.Main;
import com.kryeit.auth.LevelApi;
import org.jdbi.v3.core.Jdbi;

public class CurrencyService {
    private final Jdbi jdbi;

    public CurrencyService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Currencies updateExperience(long userId, int amount) {
        LevelApi.modifyLevel(userId, amount);
        Currencies updated = jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM currencies WHERE id = :userId")
                        .bind("userId", userId)
                        .mapTo(Currencies.class)
                        .one()
        );

        // Sync the updated data
        Main.syncManager.publishUpdate(userId, "currencies", updated);
        return updated;
    }

    public Currencies updateBeans(long userId, int amount) {
        Currencies updated = jdbi.withHandle(handle ->
                handle.createQuery("UPDATE currencies SET beans = beans + :amount WHERE id = :userId RETURNING *")
                        .bind("userId", userId)
                        .bind("amount", amount)
                        .mapTo(Currencies.class)
                        .one()
        );

        // Sync the updated data
        Main.syncManager.publishUpdate(userId, "currencies", updated);
        return updated;
    }

    public Currencies updateRolls(long userId, int amount) {
        Currencies updated = jdbi.withHandle(handle ->
                handle.createQuery("UPDATE currencies SET rolls = rolls + :amount WHERE id = :userId RETURNING *")
                        .bind("userId", userId)
                        .bind("amount", amount)
                        .mapTo(Currencies.class)
                        .one()
        );

        // Sync the updated data
        Main.syncManager.publishUpdate(userId, "currencies", updated);
        return updated;
    }

    public Currencies getCurrencies(long userId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM currencies WHERE id = :userId")
                        .bind("userId", userId)
                        .mapTo(Currencies.class)
                        .one()
        );
    }
}