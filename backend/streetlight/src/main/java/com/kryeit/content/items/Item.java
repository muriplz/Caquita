package com.kryeit.content.items;

import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.*;

public abstract class Item {
    private final String id;
    private final int width;
    private final int height;
    private final Rarity rarity;
    private final ResourceType resourceType;
    private final Map<LandmarkType, DisposalOutcome> disposalOutcomes;
    private final RecyclingReward recyclingReward;
    private final Map<String, Object> properties = new HashMap<>();

    protected Item(String id, int width, int height, Rarity rarity,
                   ResourceType resourceType, Map<LandmarkType, DisposalOutcome> disposalOutcomes,
                   RecyclingReward recyclingReward) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.rarity = rarity;
        this.resourceType = resourceType;
        this.disposalOutcomes = new EnumMap<>(disposalOutcomes);
        this.recyclingReward = recyclingReward;
    }

    public String getId() { return id; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rarity getRarity() { return rarity; }
    public ResourceType getResourceType() { return resourceType; }

    public Map<LandmarkType, DisposalOutcome> getDisposalOutcomes() {
        return Map.copyOf(disposalOutcomes);
    }

    public List<LandmarkType> getCorrectDisposalLocations() {
        return disposalOutcomes.entrySet().stream()
                .filter(entry -> entry.getValue() == DisposalOutcome.CORRECT)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<LandmarkType> getDecentDisposalLocations() {
        return disposalOutcomes.entrySet().stream()
                .filter(entry -> entry.getValue() == DisposalOutcome.DECENT)
                .map(Map.Entry::getKey)
                .toList();
    }

    public RecyclingReward getRecyclingReward() { return recyclingReward; }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public DisposalOutcome getDisposalOutcome(LandmarkType landmarkType) {
        return disposalOutcomes.getOrDefault(landmarkType, DisposalOutcome.WRONG);
    }

    public int getDisposalReward(LandmarkType landmarkType) {
        DisposalOutcome outcome = getDisposalOutcome(landmarkType);
        return recyclingReward.getReward(landmarkType, outcome);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}