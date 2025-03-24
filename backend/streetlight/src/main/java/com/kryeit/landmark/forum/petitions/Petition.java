package com.kryeit.landmark.forum.petitions;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;

public record Petition(
        long id, long userId, double lat, double lon, ObjectNode landmarkInfo, boolean accepted,
        Timestamp creation, Timestamp edition) {
}
