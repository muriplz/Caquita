import { reactive, computed } from 'vue';
import SyncService from '@/js/sync/SyncService.js';
import Landmark from './Landmark.js';

// Store for landmarks
const landmarks = reactive([]);
const listeners = new Set();

const LandmarkStore = {
    init() {
        // Subscribe to landmark updates
        SyncService.subscribe('landmark', this.updateLandmarks.bind(this));
    },

    updateLandmarks(data) {
        if (!data || !Array.isArray(data)) {
            return;
        }

        // Clear existing landmarks
        landmarks.length = 0;

        // Add new landmarks with proper objects
        data.forEach(item => {
            // Convert the raw data to Landmark instance
            landmarks.push(new Landmark(
                item.id,
                item.name,
                item.latitude || item.lat,
                item.longitude || item.lon,
                item.type,
                item.experience
            ));
        });

        // Notify listeners
        this.notifyListeners();
    },

    getLandmarks() {
        return landmarks;
    },

    // Find landmark by id
    getLandmarkById(id) {
        return landmarks.find(landmark => landmark.id === id);
    },

    // Find landmarks by type
    getLandmarksByType(type) {
        return landmarks.filter(landmark => landmark.type === type);
    },

    // Get landmarks near a location
    getLandmarksNear(latitude, longitude, maxDistance = 1000) {
        return landmarks.filter(landmark =>
            landmark.distanceFrom(latitude, longitude) <= maxDistance
        );
    },

    // Subscribe to changes
    subscribe(callback) {
        listeners.add(callback);
        return () => listeners.delete(callback);
    },

    notifyListeners() {
        listeners.forEach(callback => callback(landmarks));
    },

    // Update the server about our location
    updateLocation(latitude, longitude) {
        return SyncService.sendLocationUpdate(latitude, longitude, 1.0);
    }
};

export default LandmarkStore;