// js/sync/SyncStore.js
import { reactive, ref } from 'vue';
import Level from '@/js/auth/Level.js';

console.log("SyncStore initialized");

const currencies = reactive({
    id: null,
    level: null,
    beans: 0,
    rolls: 0
});

// Using ref for level to properly track changes
const level = ref(null);

// Set of listeners for manual subscription
const listeners = new Set();

const SyncStore = {
    // Get the reactive currencies object
    getCurrencies() {
        return currencies;
    },

    // Update currencies from sync
    updateCurrencies(data) {
        if (!data) {
            return;
        }

        currencies.id = data.id || currencies.id;
        currencies.beans = data.beans ?? currencies.beans;
        currencies.rolls = data.rolls ?? currencies.rolls;

        // Handle the level object which contains level details
        if (data.level) {
            try {
                level.value = new Level(data.level._children);
            } catch (e) {
            }
        }

        // Notify listeners
        this.notifyListeners();
    },

    // Get beans
    getBeans() {
        return currencies.beans;
    },

    // Get rolls
    getRolls() {
        return currencies.rolls;
    },

    // Get level object as a ref
    getLevel() {
        return level;
    },

    // Subscribe to changes
    subscribe(callback) {
        listeners.add(callback);
        return () => listeners.delete(callback);
    },

    // Notify all listeners
    notifyListeners() {
        listeners.forEach(callback => callback(currencies));
    },

    // For debugging
    debugSetLevel(levelData) {
        level.value = new Level(levelData);
    }
};

export default SyncStore;