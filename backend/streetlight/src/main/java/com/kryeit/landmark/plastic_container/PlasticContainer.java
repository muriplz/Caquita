package com.kryeit.landmark.plastic_container;

import com.kryeit.landmark.Landmark;
import com.kryeit.landmark.LandmarkType;

public record PlasticContainer(
        long id, double lat, double lon,
        String name, String description, long author,
        boolean underground, boolean bottlenecked, boolean modern, boolean overwhelmed
) implements Landmark {

    @Override
    public LandmarkType type() {
        return LandmarkType.PLASTIC_CONTAINER;
    }
}
