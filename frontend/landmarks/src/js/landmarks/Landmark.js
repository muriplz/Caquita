class Landmark {
    constructor(id, author, name, lat, lon, type) {
        this.id = id;
        this.author = author;
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.type = type;
    }

    // Calculate distance from a point in meters
    distanceFrom(lat, lon) {
        return calculateDistance(this.latitude, this.longitude, lat, lon);
    }
}

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

export default Landmark;