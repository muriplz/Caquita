package com.kryeit.items;

import com.kryeit.recycling.ResourceType;

import java.util.List;
import java.util.Map;

public record ConfigItem(
        String id,
        String type,
        ResourceType resourceType,
        String rarity,
        List<String> right,
        Map<String, String> exp
) {
}
