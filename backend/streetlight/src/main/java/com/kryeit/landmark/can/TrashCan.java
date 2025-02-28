package com.kryeit.landmark.can;

import com.kryeit.recycling.ResourceType;

import java.util.List;

public record TrashCan(long id, ResourceType type, List<TrashFeature> features) {
}
