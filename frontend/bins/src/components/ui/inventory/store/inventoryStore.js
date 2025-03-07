import {computed, reactive} from 'vue';
import InventoryManager from '../models/InventoryManager';
import Position from '../models/Position';
import Item from '../models/Item';
import inventoryService from '../services/inventoryService';

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
    state.inventoryManager.items.forEach(({ item, position }, id) => {
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

    state.inventoryManager.items.forEach(({ item, position }) => {
        for (let y = 0; y < item.height; y++) {
            for (let x = 0; x < item.width; x++) {
                const gridX = position.x + x;
                const gridY = position.y + y;

                if (gridX < width && gridY < height) {
                    result[gridY][gridX] = {
                        itemId: item.id,
                        isOrigin: x === 0 && y === 0,
                        x, y
                    };
                }
            }
        }
    });

    return result;
});

// Actions
async function fetchInventory(force = false) {
    if (state.loading) return;

    state.loading = true;
    state.error = null;

    try {
        const inventoryData = await inventoryService.getInventory(force);
        state.inventoryManager = new InventoryManager(inventoryData);
        state.lastSyncTime = Date.now();
    } catch (error) {
        state.error = error.message || 'Failed to fetch inventory';
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
    const itemObj = item instanceof Item ? item : Item.fromJSON(item);
    const posObj = position instanceof Position ? position : new Position(position.x, position.y);

    if (!state.inventoryManager.canPlaceItem(itemObj, posObj)) {
        return false;
    }

    state.loading = true;

    try {
        await inventoryService.addItem(itemObj, posObj);

        const success = state.inventoryManager.addItem(itemObj, posObj);
        if (success) {
            state.lastSyncTime = Date.now();
        }
        return success;
    } catch (error) {
        state.error = error.message || 'Failed to add item';
        return false;
    } finally {
        state.loading = false;
    }
}

async function removeItem(itemId) {
    if (!state.inventoryManager.getItem(itemId)) {
        return false;
    }

    state.loading = true;

    try {
        await inventoryService.removeItem(itemId);

        const success = state.inventoryManager.removeItem(itemId);
        if (success) {
            state.lastSyncTime = Date.now();
        }
        return success;
    } catch (error) {
        state.error = error.message || 'Failed to remove item';
        return false;
    } finally {
        state.loading = false;
    }
}

async function moveItem(itemId, newPosition) {
    const posObj = newPosition instanceof Position ?
        newPosition : new Position(newPosition.x, newPosition.y);

    const item = state.inventoryManager.getItem(itemId);
    if (!item) {
        return false;
    }


    if (!state.inventoryManager.canPlaceItem(item, posObj, itemId)) {
        return false;
    }

    // Store original position in case we need to revert
    const originalPosition = state.inventoryManager.getItemPosition(itemId);

    // Apply change locally first
    const tempSuccess = state.inventoryManager.moveItem(itemId, posObj);
    if (!tempSuccess) {
        return false;
    }

    state.loading = true;

    try {
        // Make API call to server
        const response = await inventoryService.moveItem(itemId, posObj);

        if (response.success) {
            state.lastSyncTime = Date.now();
            return true;
        } else {
            // Revert local change if server rejected
            if (originalPosition) {
                state.inventoryManager.moveItem(itemId, originalPosition);
            }
            return false;
        }
    } catch (error) {
        state.error = error.message || 'Failed to move item';

        // Revert local change on error
        if (originalPosition) {
            state.inventoryManager.moveItem(itemId, originalPosition);
        }

        await fetchInventory(true);
        return false;
    } finally {
        state.loading = false;
    }
}

// Initialize store
function initStore() {
    fetchInventory();
}

// Export as default
export default {
    state,
    items,
    grid,
    fetchInventory,
    openInventory,
    closeInventory,
    addItem,
    removeItem,
    moveItem,
    initStore
};