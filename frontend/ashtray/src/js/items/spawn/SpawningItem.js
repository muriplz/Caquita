class SpawningItem {
    constructor(id, itemId, userId, latitude, longitude, creation, duration) {
        this.id = id;
        this.itemId = itemId;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creation = creation;
        this.duration = duration;
    }

    isExpired() {
        return new Date(this.duration) < new Date();
    }

    // Time remaining in seconds
    getTimeRemaining() {
        const now = new Date();
        const expiry = new Date(this.duration);
        const diff = (expiry - now) / 1000; // Convert ms to seconds
        return Math.max(0, Math.floor(diff));
    }

    // Calculate distance from a point in meters
    distanceFrom(lat, lon) {
        return calculateDistance(this.latitude, this.longitude, lat, lon);
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

export default SpawningItem;