import {computed, reactive} from 'vue';
import InventoryManager from '../models/InventoryManager';
import Position from '../models/Position';
import InventoryItem from '../models/InventoryItem';
import inventoryService from '../services/inventoryService';
import ItemUtils from '../ItemUtils';

// Create a reactive state object
const state = reactive({
    inventoryManager: new InventoryManager({ width: 4, height: 4 }),
    loading: false,
    error: null,
    lastSyncTime: null,
    isOpen: false
});

// Computed properties
const items = computed(() => {
    const result = [];
    state.inventoryManager.items.forEach(({ item, position }, instanceId) => {
        result.push({
            ...item,
            position: { ...position }
        });
    });
    return result;
});

const grid = computed(() => {
    const { width, height } = state.inventoryManager;
    const result = Array(height).fill().map(() => Array(width).fill(null));

    state.inventoryManager.items.forEach(({ item, position }, instanceId) => {
        for (let y = 0; y < item.height; y++) {
            for (let x = 0; x < item.width; x++) {
                const gridX = position.x + x;
                const gridY = position.y + y;

                if (gridX < width && gridY < height) {
                    result[gridY][gridX] = {
                        instanceId: item.instanceId,
                        itemId: item.itemId,
                        isOrigin: x === 0 && y === 0,
                        x, y
                    };
                }
            }
        }
    });

    return result;
});

async function fetchInventory(force = false) {
    if (state.loading) return;

    state.loading = true;
    state.error = null;

    try {
        // Initialize ItemUtils first to ensure translations are loaded
        await ItemUtils.init();

        const inventoryData = await inventoryService.getInventory(force);

        state.inventoryManager = new InventoryManager(inventoryData);
        state.lastSyncTime = Date.now();
    } catch (error) {
        state.error = error.message || 'Failed to fetch inventory';
        console.error('Error fetching inventory:', error);
    } finally {
        state.loading = false;
    }
}

async function openInventory() {
    state.isOpen = true;
    await fetchInventory();
}

function closeInventory() {
    state.isOpen = false;
}

async function addItem(item, position) {
    let itemObj;

    if (item instanceof InventoryItem) {
        itemObj = item;
    } else {
        // Generate a temporary UUID for client-side use before server response
        const tempInstanceId = 'temp-' + Math.random().toString(36).substring(2, 15);
        itemObj = new InventoryItem(
            tempInstanceId,
            item.id || item.itemId,
            null,
            item.width,
            item.height,
            null,
            item.rarity
        );
    }

    const posObj = position instanceof Position ? position : new Position(position.x, position.y);

    if (!state.inventoryManager.canPlaceItem(itemObj, posObj)) {
        return false;
    }

    // Apply optimistic update
    const optimisticSuccess = state.inventoryManager.addItem(itemObj, posObj);
    if (!optimisticSuccess) {
        return false;
    }

    state.loading = true;

    try {
        const response = await inventoryService.addItem(itemObj, posObj);

        if (response.success) {
            // After successful server update, refresh inventory to get the real instanceId
            await fetchInventory(true);
            return true;
        } else {
            // Revert optimistic update
            state.inventoryManager.removeItem(itemObj.instanceId);
            return false;
        }
    } catch (error) {
        // Revert optimistic update
        state.inventoryManager.removeItem(itemObj.instanceId);
        state.error = error.message || 'Failed to add item';
        return false;
    } finally {
        state.loading = false;
    }
}

async function removeItem(instanceId) {
    const itemData = state.inventoryManager.items.get(instanceId);
    if (!itemData) {
        return false;
    }

    // Apply optimistic update
    const originalItem = { ...itemData.item };
    const originalPosition = { ...itemData.position };
    const success = state.inventoryManager.removeItem(instanceId);

    if (!success) {
        return false;
    }

    state.loading = true;

    try {
        const response = await inventoryService.removeItem(instanceId);

        if (response.success) {
            return true;
        } else {
            // Revert optimistic update
            state.inventoryManager.addItem(originalItem, originalPosition);
            return false;
        }
    } catch (error) {
        // Revert optimistic update
        state.inventoryManager.addItem(originalItem, originalPosition);
        state.error = error.message || 'Failed to remove item';
        return false;
    } finally {
        state.loading = false;
    }
}

async function moveItem(instanceId, newPosition) {
    const posObj = newPosition instanceof Position ?
        newPosition : new Position(newPosition.x, newPosition.y);

    const itemData = state.inventoryManager.items.get(instanceId);
    if (!itemData) {
        return false;
    }

    // Store original position in case we need to revert
    const originalPosition = { ...itemData.position };

    // Apply optimistic update
    const tempSuccess = state.inventoryManager.moveItem(instanceId, posObj);
    if (!tempSuccess) {
        return false;
    }

    state.loading = true;

    try {
        const response = await inventoryService.moveItem(instanceId, posObj);

        if (response.success) {
            return true;
        } else {
            // Revert optimistic update
            state.inventoryManager.moveItem(instanceId, originalPosition);
            return false;
        }
    } catch (error) {
        // Revert optimistic update
        state.inventoryManager.moveItem(instanceId, originalPosition);
        state.error = error.message || 'Failed to move item';
        await fetchInventory(true);
        return false;
    } finally {
        state.loading = false;
    }
}

export default {
    state,
    items,
    grid,
    fetchInventory,
    openInventory,
    closeInventory,
    addItem,
    removeItem,
    moveItem
};