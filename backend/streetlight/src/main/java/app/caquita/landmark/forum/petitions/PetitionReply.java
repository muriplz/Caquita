package app.caquita.landmark.forum.petitions;

import java.sql.Timestamp;

public record PetitionReply(long id, long messsageId, long userId, String content, Timestamp creation) {
}
