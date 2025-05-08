package com.kryeit.landmark.forum.petitions;

import java.sql.Timestamp;

public record PetitionMessage(long id, long petitionId, long userId, String content, Timestamp creation) {
}
