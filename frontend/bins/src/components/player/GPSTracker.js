import {ref, watch} from 'vue';
import {latLonToWorld, resetWorldOrigin, setWorldOrigin} from '@/js/map/TileConversion.js';
import {position, positionData} from './playerControls.js';
import settingsStore from '@/components/ui/settings/settings.js';

// Status tracking
const isTracking = ref(false);
const lastUpdateTime = ref(0);
const updateCount = ref(0);

// Resources
let watchId = null;
let keepAliveInterval = null;
let firstPosition = null;

// Process GPS coordinates
function processPosition(gpsPosition) {
    if (!gpsPosition || !gpsPosition.coords || !settingsStore.settings.general.gpsEnabled) return;

    const { latitude, longitude } = gpsPosition.coords;

    // First GPS position becomes origin
    if (!firstPosition) {
        // Store first position
        firstPosition = { lat: latitude, lon: longitude };

        // Set world origin for compatibility with existing code
        setWorldOrigin(latitude, longitude);

        // First position is always 0,0 in player coordinates
        positionData.x = 0;
        positionData.z = 0;
        position.set(0, positionData.y, 0);
    } else {
        // Calculate position relative to first position
        const worldPos = latLonToWorld(latitude, longitude);

        // Update player position
        positionData.x = worldPos.x;
        positionData.z = worldPos.z;
        position.set(worldPos.x, positionData.y, worldPos.z);
    }

    // Track updates
    updateCount.value++;
    lastUpdateTime.value = Date.now();
}

// Start GPS tracking
function startGPSTracking() {
    if (!navigator.geolocation) {
        settingsStore.settings.general.gpsEnabled = false;
        return false;
    }

    if (isTracking.value) return true;

    // Reset tracking state
    firstPosition = null;

    // Get current position
    navigator.geolocation.getCurrentPosition(
        (initialPosition) => {
            // Process the initial position
            processPosition(initialPosition);

            // Set up continuous tracking
            watchId = navigator.geolocation.watchPosition(
                processPosition,
                (error) => {
                    if (error.code === error.PERMISSION_DENIED) {
                        settingsStore.settings.general.gpsEnabled = false;
                        stopGPSTracking();
                    }
                },
                { enableHighAccuracy: true, timeout: 30000, maximumAge: 5000 }
            );

            isTracking.value = true;

            // Backup polling for more reliable updates
            keepAliveInterval = setInterval(() => {
                if (!settingsStore.settings.general.gpsEnabled) {
                    stopGPSTracking();
                    return;
                }

                if (isTracking.value) {
                    navigator.geolocation.getCurrentPosition(
                        processPosition,
                        () => {}, // Ignore errors
                        { timeout: 10000, maximumAge: 5000 }
                    );
                }
            }, 10000);
        },
        (error) => {
            if (error.code === error.PERMISSION_DENIED) {
                settingsStore.settings.general.gpsEnabled = false;
            }
        },
        { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 }
    );

    return true;
}

// Stop GPS tracking
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
}

// Reset to default position
function returnToDefaultPosition() {
    // Reset TileConversion world origin
    resetWorldOrigin();
    firstPosition = null;

    // Reset player position
    positionData.x = 0;
    positionData.z = 0;
    position.set(0, positionData.y, 0);
}

// Get status for debugging
function getGPSStatus() {
    return {
        isTracking: isTracking.value,
        firstPosition,
        playerPosition: {
            x: positionData.x,
            z: positionData.z
        },
        updateCount: updateCount.value,
        lastUpdate: lastUpdateTime.value ? new Date(lastUpdateTime.value).toISOString() : 'never'
    };
}

// Watch settings changes
watch(() => settingsStore.settings.general.gpsEnabled, (enabled) => {
    if (enabled) {
        startGPSTracking();
    } else {
        stopGPSTracking();
        returnToDefaultPosition();
    }
});

// Initialize
function initGPSSystem() {
    if (settingsStore.settings.general.gpsEnabled) {
        setTimeout(startGPSTracking, 1000);
    }
}

export {
    isTracking,
    startGPSTracking,
    stopGPSTracking,
    getGPSStatus,
    updateCount,
    lastUpdateTime,
    initGPSSystem
};