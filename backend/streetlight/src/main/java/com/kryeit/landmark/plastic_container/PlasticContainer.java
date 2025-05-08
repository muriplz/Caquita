package com.kryeit.landmark.plastic_container;

import com.kryeit.recycling.ResourceType;

import java.util.List;

public record PlasticContainer(
        long id, double lat, double lon,
        String name, String description, long author,
        boolean underground, boolean bottlenecked, boolean modern, boolean overwhelmed
) {

    public static final List<ResourceType> BEST = List.of(
            ResourceType.PLASTIC
    );

    public static final List<ResourceType> WORST = List.of(
            ResourceType.PLASTIC
    );
}
