package com.kryeit.landmark.trash_can;

import com.kryeit.landmark.Landmark;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.ResourceType;

import java.util.List;

public record TrashCan(
        long id, double lat, double lon,
        String name, String description, long author,
        boolean broken,
        boolean ashtray, boolean windblown, boolean flooded, boolean overwhelmed, boolean poopbag, boolean art
) implements Landmark {
    public static final List<ResourceType> GOOD = List.of(
            ResourceType.GREASY_CARDBOARD
    );


    @Override
    public LandmarkType type() {
        return LandmarkType.TRASH_CAN;
    }
}
