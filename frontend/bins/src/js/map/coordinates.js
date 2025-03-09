export const defaultCoordinates = {
    lat: 43.4623,
    lon: -3.8098
};

let currentCoordinates = {
    lat: defaultCoordinates.lat,
    lon: defaultCoordinates.lon,
    isCustom: false
};

export function setCoordinates(lat, lon) {
    currentCoordinates = {
        lat: Number(lat),
        lon: Number(lon),
        isCustom: true
    };

    return currentCoordinates;
}

export function resetCoordinates() {
    currentCoordinates = {
        lat: defaultCoordinates.lat,
        lon: defaultCoordinates.lon,
        isCustom: false
    };

    return currentCoordinates;
}

export function getCoordinates() {
    return currentCoordinates;
}