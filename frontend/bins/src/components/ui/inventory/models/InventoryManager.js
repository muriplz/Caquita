import Position from './Position';
import Item from './Item';

export default class InventoryManager {
    constructor(inventoryData = null) {
        this.id = inventoryData?.id || 0;
        this.userId = inventoryData?.userId || 0;
        this.width = inventoryData?.width || 4;
        this.height = inventoryData?.height || 4;
        this.items = new Map();

        if (inventoryData?.itemPositions) {
            this.loadItems(inventoryData.itemPositions);
        }
    }

    loadItems(itemPositionsData) {
        this.items.clear();

        // Handle array format
        if (Array.isArray(itemPositionsData)) {
            itemPositionsData.forEach(entry => {
                if (entry.item && entry.position) {
                    const item = new Item(
                        entry.item.id,
                        entry.item.name,
                        entry.item.width,
                        entry.item.height
                    );
                    const position = new Position(entry.position.x, entry.position.y);
                    this.items.set(item.id, { item, position });
                }
            });
        } else if (typeof itemPositionsData === 'object') {
            // For backward compatibility, handle object format
            Object.entries(itemPositionsData).forEach(([key, value]) => {
                try {
                    // Skip Java object references
                    if (key.includes("com.kryeit")) {
                        console.warn("Skipping Java object reference:", key);
                        return;
                    }

                    let item;
                    let position;

                    if (typeof key === 'string' && key.startsWith('{')) {
                        // Handle JSON string for item
                        const itemData = JSON.parse(key);
                        item = new Item(
                            itemData.id,
                            itemData.name,
                            itemData.width,
                            itemData.height
                        );
                    } else {
                        // Use the key as ID
                        item = new Item(key, "Unknown Item", 1, 1);
                    }

                    position = new Position(
                        value.x || 0,
                        value.y || 0
                    );

                    this.items.set(item.id, { item, position });
                } catch (error) {
                    console.error('Error parsing item data:', error, key);
                }
            });
        }
    }

    getItem(itemId) {
        return this.items.get(itemId)?.item || null;
    }

    getItemPosition(itemId) {
        return this.items.get(itemId)?.position || null;
    }

    getItemsAt(position) {
        const result = [];

        this.items.forEach(({ item, position: itemPos }, itemId) => {
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

    canPlaceItem(item, position, skipItemId = null) {
        if (!this.isPositionValid(position) ||
            position.x + item.width > this.width ||
            position.y + item.height > this.height) {
            return false;
        }

        for (const [itemId, { item: existingItem, position: existingPos }] of this.items.entries()) {
            if (skipItemId === itemId) continue;

            if (this.doItemsOverlap(
                item, position,
                existingItem, existingPos
            )) {
                return false;
            }
        }

        return true;
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

        this.items.set(item.id, { item, position });
        return true;
    }

    removeItem(itemId) {
        return this.items.delete(itemId);
    }

    moveItem(itemId, newPosition) {
        const itemData = this.items.get(itemId);
        if (!itemData) return false;

        if (!this.canPlaceItem(itemData.item, newPosition, itemId)) {
            return false;
        }

        itemData.position = newPosition;
        return true;
    }

    toServerFormat() {
        const itemPositions = [];

        this.items.forEach(({ item, position }) => {
            itemPositions.push({
                item: item.toJSON(),
                position: { x: position.x, y: position.y }
            });
        });

        return {
            id: this.id,
            userId: this.userId,
            width: this.width,
            height: this.height,
            itemPositions
        };
    }
}