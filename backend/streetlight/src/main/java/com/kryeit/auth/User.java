package com.kryeit.auth;

import com.kryeit.Database;

import java.sql.Timestamp;

// TODO: Add PGImage avatar
public record User(long id, String username, String password, Timestamp creation, TrustLevel trust) {

    public void changeTrust(TrustLevel trust) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE users SET trust = :trust WHERE id = :id")
                        .bind("trust", trust.toString())
                        .bind("id", this.id)
                        .execute()
        );
    }
}