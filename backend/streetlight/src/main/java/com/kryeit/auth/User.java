package com.kryeit.auth;

import com.kryeit.Database;
import com.kryeit.auth.inventory.GridInventory;

import java.sql.Timestamp;

public record User(long id, String username, String password, Timestamp creation, TrustLevel trust) {
}