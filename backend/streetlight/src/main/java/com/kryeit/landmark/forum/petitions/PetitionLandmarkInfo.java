package com.kryeit.landmark.forum.petitions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kryeit.landmark.LandmarkType;

public record PetitionLandmarkInfo(
        @JsonProperty("name") String name,
        @JsonProperty("type") LandmarkType type
) {
}
