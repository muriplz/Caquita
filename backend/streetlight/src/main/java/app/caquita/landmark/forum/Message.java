package app.caquita.landmark.forum;

import app.caquita.landmark.LandmarkType;

import java.sql.Timestamp;

public record Message(long id, long landmarkId, LandmarkType type, long userId, String content, Timestamp creation) {
}
