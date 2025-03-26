package com.kryeit.landmark;

public record Landmark(long id, String name, double lat, double lon, LandmarkType type, int experience) {

}