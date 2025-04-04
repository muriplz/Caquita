import { reactive } from 'vue';
import SyncService from '@/js/sync/SyncService.js';
import TrashCan from './TrashCan.js';
import LandmarkStore from '../LandmarkStore.js';

// Store for trash cans
const trashCans = reactive([]);
const listeners = new Set();

const TrashCanStore = {
    init() {
        // Subscribe to trash can updates
        SyncService.subscribe('trash-can', this.updateTrashCans.bind(this));
    },

    updateTrashCans(data) {
        if (!data || !Array.isArray(data)) {
            return;
        }

        // Clear existing trash cans
        trashCans.length = 0;

        // Add new trash cans with proper objects
        data.forEach(item => {
            const trashCan = new TrashCan(
                item.id,
                item.type,
                item.features
            );

            // If we have a corresponding landmark, set the coordinates directly
            const landmark = LandmarkStore.getLandmarkById(item.id);
            if (landmark) {
                trashCan.setCoordinates(landmark.latitude, landmark.longitude);
            }

            trashCans.push(trashCan);
        });

        // Notify listeners
        this.notifyListeners();
    },

    getTrashCans() {
        console.log(trashCans);
        return trashCans;
    },

    // Find trash can by id
    getTrashCanById(id) {
        return trashCans.find(trashCan => trashCan.id === id);
    },

    // Find trash cans by type
    getTrashCansByType(type) {
        return trashCans.filter(trashCan => trashCan.type === type);
    },

    // Get trash cans near a location
    async getTrashCansNear(latitude, longitude, maxDistance = 500) {
        const nearbyTrashCans = [];

        // Filter asynchronously since distanceFrom is async
        for (const trashCan of trashCans) {
            const distance = await trashCan.distanceFrom(latitude, longitude);
            if (distance <= maxDistance) {
                nearbyTrashCans.push(trashCan);
            }
        }

        return nearbyTrashCans;
    },

    // Subscribe to changes
    subscribe(callback) {
        listeners.add(callback);
        return () => listeners.delete(callback);
    },

    notifyListeners() {
        listeners.forEach(callback => callback(trashCans));
    },

    // Update the server about our location
    updateLocation(latitude, longitude) {
        return SyncService.sendLocationUpdate(latitude, longitude, 0.5);
    }
};

export default TrashCanStore;