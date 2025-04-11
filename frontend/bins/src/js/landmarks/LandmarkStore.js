import { reactive, computed } from 'vue';
import SyncService from '@/js/sync/SyncService.js';
import Landmark from './Landmark.js';

const landmarks = reactive([]);
const listeners = new Set();

const LandmarkStore = {
    init() {
        SyncService.subscribe('landmark', this.updateLandmarks.bind(this));
    },

    updateLandmarks(data) {
        if (!data || !Array.isArray(data)) {
            return;
        }

        // Clear existing landmarks
        landmarks.length = 0;

        data.forEach(item => {
            landmarks.push(new Landmark(
                item.id,
                item.author,
                item.name,
                item.latitude || item.lat,
                item.longitude || item.lon,
                item.type,
                item.experience
            ));
        });

        this.notifyListeners();
    },

    getLandmarks() {
        return landmarks;
    },

    getLandmarkById(id) {
        return landmarks.find(landmark => landmark.id === id);
    },

    getLandmarksByType(type) {
        return landmarks.filter(landmark => landmark.type === type);
    },

    getLandmarksNear(latitude, longitude, maxDistance = 1000) {
        return landmarks.filter(landmark =>
            landmark.distanceFrom(latitude, longitude) <= maxDistance
        );
    },

    subscribe(callback) {
        listeners.add(callback);
        return () => listeners.delete(callback);
    },

    notifyListeners() {
        listeners.forEach(callback => callback(landmarks));
    },

    updateLocation(latitude, longitude) {
        return SyncService.sendLocationUpdate(latitude, longitude, 1.0);
    }
};

export default LandmarkStore;