package com.kryeit.auth.inventory;

import com.kryeit.content.items.Item;
import com.kryeit.content.items.Rarity;
import java.util.Objects;
import java.util.UUID;

public class InventoryItem {
    private final String instanceId;
    private final Item item;

    public InventoryItem(Item item) {
        this.instanceId = UUID.randomUUID().toString();
        this.item = item;
    }

    public InventoryItem(String instanceId, Item item) {
        this.instanceId = instanceId;
        this.item = item;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Item getItem() {
        return item;
    }

    public String getItemId() {
        return item.getId();
    }

    public int getWidth() {
        return item.getWidth();
    }

    public int getHeight() {
        return item.getHeight();
    }

    public Rarity getRarity() {
        return item.getRarity();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return Objects.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }
}