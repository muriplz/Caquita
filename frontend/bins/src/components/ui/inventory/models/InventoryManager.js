import Position from './Position';
import InventoryItem from './InventoryItem';

export default class InventoryManager {
    constructor(inventoryData = null) {
        this.id = inventoryData?.id || 0;
        this.userId = inventoryData?.userId || 0;
        this.width = inventoryData?.width || 4;
        this.height = inventoryData?.height || 4;
        this.items = new Map(); // Key is instanceId

        // Support both property names for compatibility with backend
        if (inventoryData?.itemPlacements && Array.isArray(inventoryData.itemPlacements)) {
            this.loadItems(inventoryData.itemPlacements);
        } else if (inventoryData?.itemPositions && Array.isArray(inventoryData.itemPositions)) {
            this.loadItems(inventoryData.itemPositions);
        } else if (inventoryData?.items && Array.isArray(inventoryData.items)) {
            this.loadItems(inventoryData.items);
        }
    }

    loadItems(itemsData) {
        this.items.clear();

        itemsData.forEach(entry => {
            if (entry.item && entry.position) {
                // Generate instanceId if not provided by backend
                const instanceId = entry.instanceId || `item-${entry.item.id}-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;

                // Create new inventory item with proper parameters
                const item = new InventoryItem(
                    instanceId,
                    entry.item.id,
                    null, // name will be auto-generated from ID
                    entry.item.width,
                    entry.item.height,
                    null, // no image URL
                    entry.item.rarity
                );
                const position = new Position(entry.position.x, entry.position.y);
                this.items.set(item.instanceId, { item, position });
            }
        });
    }

    getItemByInstanceId(instanceId) {
        return this.items.get(instanceId)?.item || null;
    }

    findItemsByItemId(itemId) {
        const result = [];
        this.items.forEach(({ item }) => {
            if (item.itemId === itemId) {
                result.push(item);
            }
        });
        return result;
    }

    getPosition(instanceId) {
        return this.items.get(instanceId)?.position || null;
    }

    getItemsAt(position) {
        const result = [];

        this.items.forEach(({ item, position: itemPos }, instanceId) => {
            if (this.isPositionWithinItem(position, item, itemPos)) {
                result.push(item);
            }
        });

        return result;
    }

    isPositionWithinItem(position, item, itemPosition) {
        return (
            position.x >= itemPosition.x &&
            position.x < itemPosition.x + item.width &&
            position.y >= itemPosition.y &&
            position.y < itemPosition.y + item.height
        );
    }

    isPositionValid(position) {
        return (
            position.x >= 0 &&
            position.y >= 0 &&
            position.x < this.width &&
            position.y < this.height
        );
    }

    isItemFitting(item, position) {
        return position.x + item.width <= this.width &&
            position.y + item.height <= this.height;
    }

    canPlaceItem(item, position, skipInstanceId = null) {
        if (!this.isPositionValid(position) || !this.isItemFitting(item, position)) {
            return false;
        }

        for (const [instanceId, { item: existingItem, position: existingPos }] of this.items.entries()) {
            if (skipInstanceId === instanceId) continue;

            if (this.doItemsOverlap(
                item, position,
                existingItem, existingPos
            )) {
                return false; // Items overlap, can't place
            }
        }

        return true; // No overlaps, can place item
    }

    doItemsOverlap(item1, pos1, item2, pos2) {
        return (
            pos1.x < pos2.x + item2.width &&
            pos1.x + item1.width > pos2.x &&
            pos1.y < pos2.y + item2.height &&
            pos1.y + item1.height > pos2.y
        );
    }

    addItem(item, position) {
        if (!this.canPlaceItem(item, position)) {
            return false;
        }

        this.items.set(item.instanceId, { item, position });
        return true;
    }

    removeItem(instanceId) {
        return this.items.delete(instanceId);
    }

    moveItem(instanceId, newPosition) {
        const itemData = this.items.get(instanceId);
        if (!itemData) return false;

        if (!this.canPlaceItem(itemData.item, newPosition, instanceId)) {
            return false;
        }

        itemData.position = newPosition;
        return true;
    }
}