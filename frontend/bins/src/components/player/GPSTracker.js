import { ref, reactive, watch } from 'vue';
import {
    latLonToWorld,
    setWorldOrigin,
    WORLD_ORIGIN,
    resetWorldOrigin
} from '@/js/map/TileConversion.js';
import { position, positionData } from './playerControls.js';
import settingsStore from '@/components/ui/settings/settings.js';

const originPoint = reactive({
    lat: null,
    lon: null,
    isSet: false
});

const gpsWorldPosition = reactive({
    x: 0,
    z: 0,
    isActive: false
});

const isTracking = ref(false);
const lastUpdateTime = ref(0);
const updateCount = ref(0);

let watchId = null;
let keepAliveInterval = null;

function applyStoredPosition() {
    if (gpsWorldPosition.isActive) {
        positionData.x = gpsWorldPosition.x;
        positionData.z = gpsWorldPosition.z;
        position.set(gpsWorldPosition.x, positionData.y, gpsWorldPosition.z);
    }
}

function updatePlayerPosition(x, z) {
    positionData.x = x;
    positionData.z = z;
    position.set(x, positionData.y, z);

    gpsWorldPosition.x = x;
    gpsWorldPosition.z = z;
    gpsWorldPosition.isActive = true;

    updateCount.value++;
    lastUpdateTime.value = Date.now();
}

function processPosition(position) {
    if (!position || !position.coords) return;

    const { latitude, longitude } = position.coords;

    if (!originPoint.isSet) {
        resetWorldOrigin();

        const worldPos = latLonToWorld(latitude, longitude);
        updatePlayerPosition(worldPos.x, worldPos.z);

        originPoint.lat = latitude;
        originPoint.lon = longitude;
        originPoint.isSet = true;
        setWorldOrigin(latitude, longitude);
    } else {
        // Use a small threshold for floating point comparison instead of exact equality
        const epsilon = 0.0000001;
        if (Math.abs(latitude - originPoint.lat) < epsilon &&
            Math.abs(longitude - originPoint.lon) < epsilon) {
            applyStoredPosition();
            return;
        }

        const worldPos = latLonToWorld(latitude, longitude);
        updatePlayerPosition(worldPos.x, worldPos.z);
    }
}

function returnToDefaultPosition() {
    resetWorldOrigin();
    updatePlayerPosition(0, 0);

    gpsWorldPosition.isActive = false;
    originPoint.lat = null;
    originPoint.lon = null;
    originPoint.isSet = false;
}

function handlePositionError(error) {
    if (error.code !== error.TIMEOUT) {
        settingsStore.settings.general.gpsEnabled = false;
        gpsWorldPosition.isActive = false;
        isTracking.value = false;

        if (watchId) {
            navigator.geolocation.clearWatch(watchId);
            watchId = null;
        }

        if (keepAliveInterval) {
            clearInterval(keepAliveInterval);
            keepAliveInterval = null;
        }
    } else {
        applyStoredPosition();
    }
}

function startGPSTracking() {
    if (!navigator.geolocation) {
        settingsStore.settings.general.gpsEnabled = false;
        return false;
    }

    if (isTracking.value) {
        applyStoredPosition();
        return true;
    }

    navigator.geolocation.getCurrentPosition(
        (initialPosition) => {
            processPosition(initialPosition);

            watchId = navigator.geolocation.watchPosition(
                processPosition,
                handlePositionError,
                {
                    enableHighAccuracy: true,
                    timeout: 15000,
                    maximumAge: 0
                }
            );

            isTracking.value = true;

            keepAliveInterval = setInterval(() => {
                if (isTracking.value && originPoint.isSet) {
                    applyStoredPosition();

                    if (watchId) {
                        try {
                            navigator.geolocation.getCurrentPosition(
                                (pos) => {
                                    processPosition(pos);
                                },
                                () => {
                                    applyStoredPosition();
                                },
                                { timeout: 5000, maximumAge: 0 }
                            );
                        } catch (e) {
                            applyStoredPosition();
                        }
                    }
                }
            }, 5000);
        },
        (error) => {
            handlePositionError(error);
        },
        {
            enableHighAccuracy: true,
            timeout: 15000,
            maximumAge: 0
        }
    );

    return true;
}

function stopGPSTracking() {
    if (watchId) {
        navigator.geolocation.clearWatch(watchId);
        watchId = null;
    }

    if (keepAliveInterval) {
        clearInterval(keepAliveInterval);
        keepAliveInterval = null;
    }

    isTracking.value = false;
    gpsWorldPosition.isActive = false;
}

function resetOriginPoint() {
    if (!navigator.geolocation) return false;

    originPoint.isSet = false;

    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                processPosition(position);
                resolve(true);
            },
            (error) => {
                handlePositionError(error);
                reject(error);
            },
            {
                enableHighAccuracy: true,
                timeout: 15000,
                maximumAge: 0
            }
        );
    });
}

function getGPSStatus() {
    return {
        isActive: isTracking.value,
        isSupported: !!navigator.geolocation,
        coords: {
            latitude: originPoint.lat,
            longitude: originPoint.lon
        },
        origin: originPoint,
        worldOrigin: WORLD_ORIGIN,
        worldPosition: gpsWorldPosition,
        updateCount: updateCount.value,
        lastUpdate: lastUpdateTime.value ? new Date(lastUpdateTime.value).toISOString() : 'never'
    };
}

// Setup watches for settings changes
watch(() => settingsStore.settings.general.gpsEnabled, (enabled) => {
    if (enabled) {
        startGPSTracking();
    } else {
        stopGPSTracking();
        returnToDefaultPosition();
    }
});

// Initialize the GPS system
function initGPSSystem() {
    if (settingsStore.settings.general.gpsEnabled) {
        setTimeout(() => {
            startGPSTracking();
        }, 500);
    }
}

export {
    gpsWorldPosition,
    isTracking,
    startGPSTracking,
    stopGPSTracking,
    resetOriginPoint,
    getGPSStatus,
    updateCount,
    lastUpdateTime,
    initGPSSystem
};