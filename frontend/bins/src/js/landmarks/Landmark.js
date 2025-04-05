class Landmark {
    constructor(id, name, lat, lon, type, experience) {
        this.id = id;
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.type = type;
        this.experience = experience;
    }

    // Calculate distance from a point in meters
    distanceFrom(lat, lon) {
        return calculateDistance(this.latitude, this.longitude, lat, lon);
    }

    // Get level information from experience
    getLevel() {
        return calculateLevelFromExperience(this.experience);
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

// Calculate level from experience points (matches backend LandmarkLevel.java)
function calculateLevelFromExperience(experience) {
    const LEVEL_REQUIRED_EXP = [
        0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000,
        55000, 65000, 75000, 85000, 100000, 120000, 140000, 160000, 185000, 210000,
        260000, 335000, 435000, 560000, 710000, 900000, 1100000, 1350000, 1650000, 2000000,
        2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000
    ];

    const MAX_LEVEL = 40;
    let level = 1;
    let totalExp = 0;

    while (level < MAX_LEVEL && totalExp + LEVEL_REQUIRED_EXP[level - 1] <= experience) {
        totalExp += LEVEL_REQUIRED_EXP[level - 1];
        level++;
    }

    const levelProgress = experience - totalExp;
    const levelTotal = level < MAX_LEVEL ? LEVEL_REQUIRED_EXP[level - 1] : LEVEL_REQUIRED_EXP[MAX_LEVEL - 1];

    return {
        level,
        experience,
        progress: levelProgress,
        total: levelTotal
    };
}

export default Landmark;