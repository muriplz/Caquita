package com.kryeit.landmark.trash_can;

import com.kryeit.landmark.Landmark;
import com.kryeit.landmark.LandmarkType;

public record TrashCan(
        long id, double lat, double lon,
        String name, String description, long author,
        boolean broken,
        boolean ashtray, boolean windblown, boolean flooded, boolean overwhelmed, boolean poopbag, boolean art
) implements Landmark {

    @Override
    public LandmarkType type() {
        return LandmarkType.TRASH_CAN;
    }
}
