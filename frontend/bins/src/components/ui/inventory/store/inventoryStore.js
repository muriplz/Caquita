import { reactive, computed } from 'vue';
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
        console.log('[Inventory Store] Loaded inventory data:', inventoryData);
        state.inventoryManager = new InventoryManager(inventoryData);
        state.lastSyncTime = Date.now();
    } catch (error) {
        state.error = error.message || 'Failed to fetch inventory';
        console.error('[Inventory Store] Error fetching inventory:', error);
    } finally {
        state.loading = false;
    }
}

async function openInventory() {
    console.log('[Inventory Store] Opening inventory');
    state.isOpen = true;
    await fetchInventory();
}

function closeInventory() {
    console.log('[Inventory Store] Closing inventory');
    state.isOpen = false;
}

async function addItem(item, position) {
    const itemObj = item instanceof Item ? item : Item.fromJSON(item);
    const posObj = position instanceof Position ? position : new Position(position.x, position.y);

    if (!state.inventoryManager.canPlaceItem(itemObj, posObj)) {
        console.log('[Inventory Store] Cannot place item at the specified position');
        return false;
    }

    state.loading = true;

    try {
        await inventoryService.addItem(itemObj, posObj);

        const success = state.inventoryManager.addItem(itemObj, posObj);
        if (success) {
            console.log('[Inventory Store] Item added successfully');
            state.lastSyncTime = Date.now();
        }
        return success;
    } catch (error) {
        state.error = error.message || 'Failed to add item';
        console.error('[Inventory Store] Error adding item:', error);
        return false;
    } finally {
        state.loading = false;
    }
}

async function removeItem(itemId) {
    if (!state.inventoryManager.getItem(itemId)) {
        console.log('[Inventory Store] Item not found:', itemId);
        return false;
    }

    state.loading = true;

    try {
        await inventoryService.removeItem(itemId);

        const success = state.inventoryManager.removeItem(itemId);
        if (success) {
            console.log('[Inventory Store] Item removed successfully');
            state.lastSyncTime = Date.now();
        }
        return success;
    } catch (error) {
        state.error = error.message || 'Failed to remove item';
        console.error('[Inventory Store] Error removing item:', error);
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
        console.log('[Inventory Store] Item not found:', itemId);
        return false;
    }

    console.log('[Inventory Store] Attempting to move item:', itemId, 'to position:', posObj);

    if (!state.inventoryManager.canPlaceItem(item, posObj, itemId)) {
        console.log('[Inventory Store] Cannot place item at the specified position');
        return false;
    }

    // Store original position in case we need to revert
    const originalPosition = state.inventoryManager.getItemPosition(itemId);

    // Apply change locally first
    const tempSuccess = state.inventoryManager.moveItem(itemId, posObj);
    if (!tempSuccess) {
        console.log('[Inventory Store] Failed to move item temporarily');
        return false;
    }

    state.loading = true;

    try {
        // Make API call to server
        console.log('[Inventory Store] Sending move request to server for item:', itemId);
        const response = await inventoryService.moveItem(itemId, posObj);

        if (response.success) {
            console.log('[Inventory Store] Item moved successfully on server');
            state.lastSyncTime = Date.now();
            return true;
        } else {
            console.log('[Inventory Store] Server rejected move operation');
            // Revert local change if server rejected
            if (originalPosition) {
                state.inventoryManager.moveItem(itemId, originalPosition);
            }
            return false;
        }
    } catch (error) {
        state.error = error.message || 'Failed to move item';
        console.error('[Inventory Store] Error moving item:', error);

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
    console.log('[Inventory Store] Initializing store');
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