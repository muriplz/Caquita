package com.kryeit.landmark.forum.petitions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.landmark.LandmarkType;

import java.sql.Timestamp;

public record Petition(
        long id, long userId, LandmarkType type, double lat, double lon, ObjectNode landmarkInfo, Status status,
        Timestamp creation, Timestamp edition) {

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
