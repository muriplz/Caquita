package com.kryeit.auth;

import java.sql.Timestamp;

public record Friendship(long id, long userId, long friendId, Status status, Timestamp creation, Timestamp edition) {
    public enum Status {
        PENDING,
        ACCEPTED,
        BLOCKED,
        REJECTED
    }
}
