package com.kryeit.landmark.can;

public record TrashFeature(Feature feature, FeatureLevel level) {

    enum Feature {
        ASHTRAY,
        INUNDATED,
        WINDBLOWN,
        OVERWHELMED,
        ;
    }

    enum FeatureLevel {
        SMALL,
        MEDIUM,
        LARGE,
        ;
    }
}
