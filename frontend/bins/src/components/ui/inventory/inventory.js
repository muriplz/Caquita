import inventoryService from './services/inventoryService.js';
import InventoryManager from './models/InventoryManager.js';
import Position from './models/Position.js';
import Item from './models/Item.js';

const debounce = (fn, delay) => {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
};

export default {
    namespaced: true,

    state: {
        inventoryManager: new InventoryManager({ width: 4, height: 4 }),
        loading: false,
        error: null,
        isDirty: false,
        lastSyncTime: null,
        isOpen: false,
    },

    getters: {
        items: state => {
            const items = [];
            state.inventoryManager.items.forEach(({ item, position }, id) => {
                items.push({
                    ...item,
                    position: { ...position }
                });
            });
            return items;
        },

        grid: state => {
            const { width, height } = state.inventoryManager;
            const grid = Array(height).fill().map(() => Array(width).fill(null));

            state.inventoryManager.items.forEach(({ item, position }) => {
                for (let y = 0; y < item.height; y++) {
                    for (let x = 0; x < item.width; x++) {
                        const gridX = position.x + x;
                        const gridY = position.y + y;

                        if (gridX < width && gridY < height) {
                            grid[gridY][gridX] = {
                                itemId: item.id,
                                isOrigin: x === 0 && y === 0,
                                x, y
                            };
                        }
                    }
                }
            });

            return grid;
        },

        isLoaded: state => state.lastSyncTime !== null,
        canSync: state => state.isDirty && !state.loading,
    },

    mutations: {
        SET_INVENTORY(state, inventoryData) {
            state.inventoryManager = new InventoryManager(inventoryData);
            state.lastSyncTime = Date.now();
            state.isDirty = false;
        },

        SET_LOADING(state, isLoading) {
            state.loading = isLoading;
        },

        SET_ERROR(state, error) {
            state.error = error;
        },

        SET_INVENTORY_OPEN(state, isOpen) {
            state.isOpen = isOpen;
        },

        ADD_ITEM(state, { item, position }) {
            const itemObj = item instanceof Item ? item : Item.fromJSON(item);
            const posObj = position instanceof Position ?
                position : new Position(position.x, position.y);

            const success = state.inventoryManager.addItem(itemObj, posObj);
            if (success) {
                state.isDirty = true;
            }

            return success;
        },

        REMOVE_ITEM(state, itemId) {
            const success = state.inventoryManager.removeItem(itemId);
            if (success) {
                state.isDirty = true;
            }

            return success;
        },

        MOVE_ITEM(state, { itemId, newPosition }) {
            const posObj = newPosition instanceof Position ?
                newPosition : new Position(newPosition.x, newPosition.y);

            const success = state.inventoryManager.moveItem(itemId, posObj);
            if (success) {
                state.isDirty = true;
            }

            return success;
        },

        MARK_SYNCED(state) {
            state.isDirty = false;
            state.lastSyncTime = Date.now();
        }
    },

    actions: {
        async fetchInventory({ commit, state, dispatch }, { force = false } = {}) {
            if (state.loading) return;

            if (!force && state.lastSyncTime !== null && !state.isOpen) {
                return;
            }

            commit('SET_LOADING', true);
            commit('SET_ERROR', null);

            try {
                const inventoryData = await inventoryService.getInventory(force);
                commit('SET_INVENTORY', inventoryData);
            } catch (error) {
                commit('SET_ERROR', error.message || 'Failed to fetch inventory');
                console.error('Error fetching inventory:', error);
            } finally {
                commit('SET_LOADING', false);
            }
        },

        openInventory({ commit, dispatch }) {
            commit('SET_INVENTORY_OPEN', true);
            dispatch('fetchInventory');
        },

        closeInventory({ commit }) {
            commit('SET_INVENTORY_OPEN', false);
        },

        async addItem({ commit, dispatch }, { item, position }) {
            const success = commit('ADD_ITEM', { item, position });
            if (success) {
                dispatch('syncInventory');
            }
            return success;
        },

        async removeItem({ commit, dispatch }, itemId) {
            const success = commit('REMOVE_ITEM', itemId);
            if (success) {
                dispatch('syncInventory');
            }
            return success;
        },

        async moveItem({ commit, dispatch, state }, { itemId, newPosition }) {
            const success = commit('MOVE_ITEM', { itemId, newPosition });
            if (success) {
                inventoryService.updateLocalInventory(state.inventoryManager);
                dispatch('debouncedSync');
            }
            return success;
        },

        async syncInventory({ commit, state, dispatch }) {
            if (!state.isDirty || state.loading) return;

            commit('SET_LOADING', true);

            try {
                const firstItem = Array.from(state.inventoryManager.items.keys())[0];

                if (!firstItem) {
                    commit('MARK_SYNCED');
                    return;
                }

                await inventoryService.moveItem(
                    firstItem,
                    { x: 0, y: 0 }
                );
                commit('MARK_SYNCED');
            } catch (error) {
                commit('SET_ERROR', error.message || 'Failed to sync inventory');
                console.error('Error syncing inventory:', error);
                dispatch('fetchInventory', { force: true });
            } finally {
                commit('SET_LOADING', false);
            }
        },

        debouncedSync: debounce(function({ dispatch }) {
            dispatch('syncInventory');
        }, 500)
    }
};