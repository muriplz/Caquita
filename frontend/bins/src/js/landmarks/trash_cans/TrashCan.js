class TrashCan {
    constructor(id, type, features) {
        this.id = id;
        this.type = type;
        this.features = features;
        this._latitude = null;
        this._longitude = null;
    }

    // Lazy-load coordinates from parent landmark when needed
    async getCoordinates() {
        if (this._latitude !== null && this._longitude !== null) {
            return {
                latitude: this._latitude,
                longitude: this._longitude
            };
        }

        // Get coordinates from the landmark store
        // This would typically come from the LandmarkStore
        const landmarkStore = await import('../LandmarkStore.js').then(m => m.default);
        const landmark = landmarkStore.getLandmarkById(this.id);

        if (landmark) {
            this._latitude = landmark.latitude;
            this._longitude = landmark.longitude;
            return {
                latitude: this._latitude,
                longitude: this._longitude
            };
        }

        return null;
    }

    // Set coordinates directly (useful when we have them)
    setCoordinates(lat, lon) {
        this._latitude = lat;
        this._longitude = lon;
    }

    // Calculate distance from a point in meters
    async distanceFrom(lat, lon) {
        const coords = await this.getCoordinates();
        if (!coords) return Infinity;

        return calculateDistance(coords.latitude, coords.longitude, lat, lon);
    }

    // Get a feature value with optional default
    getFeature(key, defaultValue = null) {
        return this.features && this.features[key] !== undefined ?
            this.features[key] : defaultValue;
    }
}

// Helper function to calculate distance between coordinates in meters
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth radius in meters
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}

export default TrashCan;