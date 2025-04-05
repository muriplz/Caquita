import { reactive, computed } from 'vue';
import SyncService from '@/js/sync/SyncService.js';
import SpawningItem from './SpawningItem.js';

// Store for spawning items
const items = reactive([]);
const listeners = new Set();

const SpawningItemStore = {
    init() {
        // Subscribe to spawning item updates
        SyncService.subscribe('spawning-item', this.updateItems.bind(this));
    },

    updateItems(data) {
        if (!data || !Array.isArray(data)) {
            return;
        }

        // Clear existing items
        items.length = 0;

        // Add new items with proper objects
        data.forEach(item => {
            // Convert the raw data to SpawningItem instance
            items.push(new SpawningItem(
                item.id,
                item.itemId,
                item.userId,
                item.latitude || item.lat, // Support both formats
                item.longitude || item.lon, // Support both formats
                item.creation,
                item.duration
            ));
        });

        // Notify listeners
        this.notifyListeners();
    },

    getItems() {
        // Return only non-expired items
        return items.filter(item => !item.isExpired());
    },

    // Find items by itemId
    getItemsByType(itemId) {
        return this.getItems().filter(item => item.itemId === itemId);
    },

    // Get items near a location
    getItemsNear(latitude, longitude, maxDistance = 100) {
        return this.getItems().filter(item =>
            item.distanceFrom(latitude, longitude) <= maxDistance
        );
    },

    // Subscribe to changes
    subscribe(callback) {
        listeners.add(callback);
        return () => listeners.delete(callback);
    },

    notifyListeners() {
        listeners.forEach(callback => callback(this.getItems()));
    },

    // Update the server about our location
    updateLocation(latitude, longitude) {
        return SyncService.sendLocationUpdate(latitude, longitude, 1.0);
    }
};

export default SpawningItemStore;