package com.kryeit.landmark.forum.petitions;

import java.sql.Timestamp;

public record Message(long id, long petitionId, long userId, String content, Timestamp creation) {
}
