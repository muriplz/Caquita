package com.kryeit.landmark.plastic_container;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.recycling.ResourceType;

public record PlasticContainer(long id, ResourceType type, ObjectNode features) {
}
