package com.kryeit.spawning;

import com.kryeit.Main;
import com.kryeit.auth.LevelApi;
import com.kryeit.auth.currency.Currencies;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class SpawningItemService {
    private final Jdbi jdbi;

    public SpawningItemService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<SpawningItem> removeSpawningItem(long userId, double lat, double lon) {
        SpawningItem.delete(userId, lat, lon);

        List<SpawningItem> updated = SpawningItem.get(userId, lat, lon);

        Main.syncManager.publishUpdate(userId, "spawning-item", updated);
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