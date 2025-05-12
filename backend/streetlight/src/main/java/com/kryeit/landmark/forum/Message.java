package com.kryeit.landmark.forum;

import com.kryeit.landmark.LandmarkType;

import java.sql.Timestamp;

public record Message(long id, long landmarkId, LandmarkType type, long userId, String content, Timestamp creation) {
}
