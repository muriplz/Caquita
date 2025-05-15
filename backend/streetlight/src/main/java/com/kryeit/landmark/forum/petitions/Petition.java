package com.kryeit.landmark.forum.petitions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.landmark.LandmarkType;

import java.sql.Timestamp;
import java.util.HashMap;

public record Petition(
        long id, String description, long userId, LandmarkType type,
        double lat, double lon, HashMap<String, Boolean> info,
        Status status, Timestamp creation, Timestamp edition
) {

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}