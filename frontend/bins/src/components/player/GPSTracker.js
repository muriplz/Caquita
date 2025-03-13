import {ref, watch} from 'vue';
import {latLonToWorld, resetWorldOrigin, setWorldOrigin} from '@/js/map/TileConversion.js';
import {position, positionData, moveTo, isMoving} from './playerControls.js';
import settingsStore from '@/components/ui/settings/settings.js';

// Status tracking
const isTracking = ref(false);
const lastUpdateTime = ref(0);
const updateCount = ref(0);

// Resources
let watchId = null;
let keepAliveInterval = null;
let firstPosition = null;
let positionBuffer = [];
const BUFFER_SIZE = 3;
const MAX_ACCURACY = 30; // Maximum acceptable accuracy in meters

// Process GPS coordinates
function processPosition(gpsPosition) {
    if (!gpsPosition || !gpsPosition.coords || !settingsStore.settings.general.gpsEnabled) return;

    const { latitude, longitude, accuracy } = gpsPosition.coords;

    // Skip highly inaccurate readings
    if (accuracy > MAX_ACCURACY) {
        console.log(`Skipping inaccurate GPS reading (${accuracy}m)`);
        return;
    }

    // First GPS position becomes origin
    if (!firstPosition) {
        // Store positions in buffer for averaging
        positionBuffer.push({ lat: latitude, lon: longitude });

        // Once we have enough readings, average them for better accuracy
        if (positionBuffer.length >= BUFFER_SIZE) {
            const avgLat = positionBuffer.reduce((sum, pos) => sum + pos.lat, 0) / positionBuffer.length;
            const avgLon = positionBuffer.reduce((sum, pos) => sum + pos.lon, 0) / positionBuffer.length;

            // Store averaged first position
            firstPosition = { lat: avgLat, lon: avgLon };

            // Set world origin for compatibility with existing code
            setWorldOrigin(avgLat, avgLon);

            // First position is always 0,0 in player coordinates
            positionData.x = 0;
            positionData.z = 0;
            position.set(0, positionData.y, 0);

            // Clear buffer
            positionBuffer = [];
        } else {
            // Wait for more readings
            return;
        }
    } else {
        // Calculate position relative to first position
        const worldPos = latLonToWorld(latitude, longitude);

        // Only start a new movement if not already moving and position changed significantly
        if (!isMoving.value) {
            const distance = Math.hypot(
                worldPos.x - positionData.x,
                worldPos.z - positionData.z
            );

            // If moved more than 1 meter, animate
            if (distance > 1) {
                moveTo(worldPos.x, positionData.y, worldPos.z, 1000);
            } else {
                // Small change, update directly
                positionData.x = worldPos.x;
                positionData.z = worldPos.z;
                position.set(worldPos.x, positionData.y, worldPos.z);
            }
        }
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
    positionBuffer = [];

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
    positionBuffer = [];

    // Reset player position with smooth animation
    if (!isMoving.value) {
        moveTo(0, positionData.y, 0, 1500); // 1.5-second animation
    }
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
        lastUpdate: lastUpdateTime.value ? new Date(lastUpdateTime.value).toISOString() : 'never',
        bufferSize: positionBuffer.length
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