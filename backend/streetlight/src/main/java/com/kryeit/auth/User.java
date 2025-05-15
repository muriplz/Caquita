package com.kryeit.auth;

import java.sql.Timestamp;

public record User(
        long id, String username, String password, Timestamp creation, Timestamp connection,
        Trust trust, String avatar) {

    public enum Trust {
        DEFAULT,
        TRUSTED,
        CONTRIBUTOR,
        MODERATOR,
        ADMINISTRATOR,
        ;
    }
}