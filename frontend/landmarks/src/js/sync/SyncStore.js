import { reactive, ref } from 'vue';
import Level from '@/js/auth/Level.ts';
import SyncService from './SyncService.js';

const currencies = reactive({
    id: null,
    level: null,
    beans: 0,
    rolls: 0
});

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
        currencies.level = new Level(data.level._children);

        this.notifyListeners();
    },

    getBeans() {
        return currencies.beans;
    },

    getRolls() {
        return currencies.rolls;
    },

    getLevel() {
        return currencies.level;
    },

    subscribe(callback) {
        listeners.add(callback);
        // Return unsubscribe function
        return () => listeners.delete(callback);
    },

    notifyListeners() {
        listeners.forEach(callback => callback(currencies));
    },

};

export default SyncStore;