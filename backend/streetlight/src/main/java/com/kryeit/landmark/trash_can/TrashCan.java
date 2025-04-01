package com.kryeit.landmark.trash_can;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kryeit.recycling.ResourceType;

public record TrashCan(long id, ResourceType type, ObjectNode features) {
}
