package app.caquita.landmark.forum;

import java.sql.Timestamp;

public record Reply(long id, long messsageId, long userId, String content, Timestamp creation) {
}
