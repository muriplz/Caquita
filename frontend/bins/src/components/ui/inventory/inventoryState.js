import {computed, reactive, readonly} from 'vue';
import InventoryManager from './models/InventoryManager.js';
import Position from './models/Position.js';
import Item from './models/Item.js';
import inventoryService from './services/inventoryService.js';

const debounce = (fn, delay) => {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
};

export function createInventoryState(gridWidth = 4, gridHeight = 4) {
    const state = reactive({
        inventoryManager: new InventoryManager({ width: gridWidth, height: gridHeight }),
        loading: false,
        error: null,
        isDirty: false,
        lastSyncTime: null,
        isOpen: false,
    });

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

    const isLoaded = computed(() => state.lastSyncTime !== null);
    const canSync = computed(() => state.isDirty && !state.loading);

    const actions = {
        async fetchInventory(options = {}) {
            const { force = false } = options;

            if (state.loading) return;

            if (!force && state.lastSyncTime !== null && !state.isOpen) {
                return;
            }

            state.loading = true;
            state.error = null;

            try {
                const inventoryData = await inventoryService.getInventory(force);
                state.inventoryManager = new InventoryManager(inventoryData);
                state.lastSyncTime = Date.now();
                state.isDirty = false;
            } catch (error) {
                state.error = error.message || 'Failed to fetch inventory';
                console.error('Error fetching inventory:', error);
            } finally {
                state.loading = false;
            }
        },

        openInventory() {
            state.isOpen = true;
            actions.fetchInventory();
        },

        closeInventory() {
            state.isOpen = false;
        },

        async addItem(item, position) {
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
                console.error('Error adding item:', error);
                return false;
            } finally {
                state.loading = false;
            }
        },

        async removeItem(itemId) {
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
                console.error('Error removing item:', error);
                return false;
            } finally {
                state.loading = false;
            }
        },

        async moveItem(itemId, newPosition) {
            const posObj = newPosition instanceof Position ?
                newPosition : new Position(newPosition.x, newPosition.y);

            const item = state.inventoryManager.getItem(itemId);
            if (!item) return false;

            if (!state.inventoryManager.canPlaceItem(item, posObj, itemId)) {
                return false;
            }

            const originalPosition = state.inventoryManager.getItemPosition(itemId);
            const tempSuccess = state.inventoryManager.moveItem(itemId, posObj);

            if (!tempSuccess) return false;

            state.loading = true;

            try {
                await inventoryService.moveItem(itemId, posObj);
                state.lastSyncTime = Date.now();
                return true;
            } catch (error) {
                state.error = error.message || 'Failed to move item';
                console.error('Error moving item:', error);

                if (originalPosition) {
                    state.inventoryManager.moveItem(itemId, originalPosition);
                }

                await actions.fetchInventory({ force: true });
                return false;
            } finally {
                state.loading = false;
            }
        }
    };

    return {
        state: readonly(state),
        items,
        grid,
        isLoaded,
        canSync,
        ...actions
    };
}