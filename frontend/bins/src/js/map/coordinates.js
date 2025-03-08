// Direct, non-reactive coordinates store
// This avoids any Vue reactivity issues

// Default values (Santander)
const defaultCoordinates = {
    lat: 43.4623,
    lon: -3.8098
};

// Our actual coordinates that will be used
let currentCoordinates = {
    lat: defaultCoordinates.lat,
    lon: defaultCoordinates.lon,
    isCustom: false
};

// Set coordinates directly
export function setCoordinates(lat, lon) {
    currentCoordinates = {
        lat: Number(lat),
        lon: Number(lon),
        isCustom: true
    };

    return currentCoordinates;
}

// Reset to default
export function resetCoordinates() {
    currentCoordinates = {
        lat: defaultCoordinates.lat,
        lon: defaultCoordinates.lon,
        isCustom: false
    };

    return currentCoordinates;
}

// Get current coordinates
export function getCoordinates() {
    return currentCoordinates;
}

// Export default values for reference
export { defaultCoordinates };