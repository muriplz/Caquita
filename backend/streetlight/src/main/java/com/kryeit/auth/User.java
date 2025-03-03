package com.kryeit.auth;

import com.kryeit.Database;
import com.kryeit.auth.inventory.GridInventory;

import java.sql.Timestamp;

public record User(long id, String username, String password, Timestamp creation, TrustLevel trust, int experience) {

    public void changeTrust(TrustLevel trust) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE users SET trust = :trust WHERE id = :id")
                        .bind("trust", trust.toString())
                        .bind("id", this.id)
                        .execute()
        );
    }
}