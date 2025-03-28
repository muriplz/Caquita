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
        console.log("getCurrencies called, returning:", currencies);
        return currencies;
    },

    // Update currencies from sync
    updateCurrencies(data) {
        console.log("updateCurrencies called with data:", data);
        if (!data) {
            console.warn("No data provided to updateCurrencies");
            return;
        }

        currencies.id = data.id || currencies.id;
        currencies.beans = data.beans ?? currencies.beans;
        currencies.rolls = data.rolls ?? currencies.rolls;

        // Handle the level object which contains level details
        if (data.level) {
            console.log("Setting level with data:", data.level);
            try {
                level.value = new Level(data.level);
                console.log("Level successfully set:", level.value);
            } catch (e) {
                console.error("Error creating Level object:", e);
            }
        } else {
            console.warn("No level data in update");
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
        console.log("getLevel called, returning:", level);
        return level;
    },

    // Subscribe to changes
    subscribe(callback) {
        listeners.add(callback);
        return () => listeners.delete(callback);
    },

    // Notify all listeners
    notifyListeners() {
        console.log("Notifying listeners, count:", listeners.size);
        listeners.forEach(callback => callback(currencies));
    },

    // For debugging
    debugSetLevel(levelData) {
        console.log("Debug setting level with:", levelData);
        level.value = new Level(levelData);
    }
};

export default SyncStore;