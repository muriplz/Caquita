package com.kryeit.landmark.forum;

import java.sql.Timestamp;

public record Message(long id, long landmarkId, long userId, String content, Timestamp creation) {
}
