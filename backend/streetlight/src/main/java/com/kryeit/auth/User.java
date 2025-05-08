package com.kryeit.auth;

import java.sql.Timestamp;

public record User(
        long id, String username, String password, Timestamp creation, Timestamp connection,
        TrustLevel trust, String avatar) {

}