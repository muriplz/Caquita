import { reactive, ref } from 'vue';
import Level from '@/js/auth/Level.js';
import SyncService from './SyncService.js';

const currencies = reactive({
    id: null,
    level: null,
    beans: 0,
    rolls: 0
});

const level = ref(null);

const listeners = new Set();

const SyncStore = {
    init() {
        // Auto-subscribe to currencies updates
        SyncService.subscribe('currencies', this.updateCurrencies.bind(this));
    },

    updateCurrencies(data) {
        if (!data) {
            return;
        }

        currencies.id = data.id || currencies.id;
        currencies.beans = data.beans ?? currencies.beans;
        currencies.rolls = data.rolls ?? currencies.rolls;

        if (data.level) {
            try {
                level.value = new Level(data.level._children);
            } catch (e) {
                console.error('Error parsing level data:', e);
            }
        }

        this.notifyListeners();
    },

    getBeans() {
        return currencies.beans;
    },

    getRolls() {
        return currencies.rolls;
    },

    getLevel() {
        return level;
    },

    subscribe(callback) {
        listeners.add(callback);
        // Return unsubscribe function
        return () => listeners.delete(callback);
    },

    notifyListeners() {
        listeners.forEach(callback => callback(currencies));
    },

    debugSetLevel(levelData) {
        level.value = new Level(levelData);
    }
};

export default SyncStore;