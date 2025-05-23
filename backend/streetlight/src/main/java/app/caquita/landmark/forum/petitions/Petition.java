package app.caquita.landmark.forum.petitions;

import app.caquita.landmark.LandmarkType;
import org.jdbi.v3.json.Json;

import java.sql.Timestamp;
import java.util.HashMap;

public record Petition(
        long id, String name, String description, long userId, LandmarkType type,
        double lat, double lon,
        @Json HashMap<String, Boolean> info,
        Status status, Timestamp creation, Timestamp edition
) {
    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}