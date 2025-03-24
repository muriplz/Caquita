package com.kryeit.content.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kryeit.landmark.LandmarkType;
import com.kryeit.recycling.DisposalOutcome;
import com.kryeit.recycling.RecyclingReward;
import com.kryeit.recycling.ResourceType;

import java.util.*;

public abstract class Item {
    private final String id;
    private final List<int[]> shape;
    private final Rarity rarity;
    private final ResourceType resourceType;
    private final Map<LandmarkType, DisposalOutcome> disposalOutcomes;
    private final RecyclingReward recyclingReward;

    private final String nbt;

    // Empty constructor for Jackson's json
    private Item() {
        this.id = "";
        this.shape = List.of();
        this.rarity = Rarity.COMMON;
        this.resourceType = ResourceType.OTHER;
        this.disposalOutcomes = Map.of();
        this.recyclingReward = RecyclingReward.builder().build();
        this.nbt = "{}";
    }

    protected Item(@JsonProperty("id") String id,
                   @JsonProperty("shape") List<int[]> shape,
                   @JsonProperty("rarity") Rarity rarity,
                   @JsonProperty("resourceType") ResourceType resourceType,
                   @JsonProperty("disposalOutcomes") Map<LandmarkType, DisposalOutcome> disposalOutcomes,
                   @JsonProperty("recyclingReward") RecyclingReward recyclingReward,
                   @JsonProperty("nbt") String nbt) {
        this.id = id;
        this.shape = shape;
        this.rarity = rarity;
        this.resourceType = resourceType;
        this.disposalOutcomes = new EnumMap<>(disposalOutcomes);
        this.recyclingReward = recyclingReward;
        this.nbt = nbt;
    }

    public String getId() { return id; }
    public List<int[]> getShape() { return shape; }
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

    public int greatestWidth() {
        return shape.stream()
                .mapToInt(row -> {
                    int maxWidth = 0;
                    int currentWidth = 0;
                    for (int cell : row) {
                        if (cell == 1) {
                            currentWidth++;
                            if (currentWidth > maxWidth) {
                                maxWidth = currentWidth;
                            }
                        } else {
                            currentWidth = 0;
                        }
                    }
                    return maxWidth;
                })
                .max()
                .orElse(0);
    }

    public int greatestHeight() {
        return (int) shape.stream()
                .filter(row -> Arrays.stream(row).anyMatch(cell -> cell == 1))
                .count();
    }
}