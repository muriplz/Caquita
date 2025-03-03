import { ref, reactive, watch } from 'vue';
import { useGeolocation } from '@vueuse/core';
import { latLonToWorld, setWorldOrigin, WORLD_ORIGIN } from '@/js/map/TileConversion.js';
import { position, positionData } from "@/components/player/playerControls.js";
import settingsStore from '@/js/settings.js';

// Origin point (first GPS position)
const originPoint = reactive({
    lat: null,
    lon: null,
    isSet: false
});

// World position derived from GPS data
const gpsWorldPosition = reactive({
    x: 0,
    z: 0,
    isActive: false
});

// Tile size for conversion
const TILE_SIZE = 80;

// Initialize the useGeolocation composable with high accuracy
const {
    coords,
    locatedAt,
    error,
    resume,
    pause,
    isSupported
} = useGeolocation({
    enableHighAccuracy: true,
    maximumAge: 0,
    timeout: 15000,
    immediate: false
});

// Set the origin point to current GPS coordinates
function setOriginPoint() {
    if (coords.value && coords.value.latitude && coords.value.longitude) {
        // Set our local origin point
        originPoint.lat = coords.value.latitude;
        originPoint.lon = coords.value.longitude;
        originPoint.isSet = true;

        // Also set the world origin (this will only happen once if not already set)
        setWorldOrigin(coords.value.latitude, coords.value.longitude);

        updateWorldPosition();
        return true;
    }
    return false;
}

// Reset the origin to current position
function resetOriginPoint() {
    return setOriginPoint();
}

// Calculate world position from GPS coordinates
function updateWorldPosition() {
    if (!coords.value || !coords.value.latitude || !coords.value.longitude) {
        return;
    }

    // Calculate world position relative to origin
    const worldPos = latLonToWorld(coords.value.latitude, coords.value.longitude, TILE_SIZE);

    // Update reactive position data
    gpsWorldPosition.x = worldPos.x;
    gpsWorldPosition.z = worldPos.z;

    return worldPos;
}

// Watch for GPS coordinates changes and update the world position
watch(coords, (newCoords) => {
    if (newCoords && newCoords.latitude && newCoords.longitude) {
        // Set origin point on first position if not set
        if (!originPoint.isSet) {
            setOriginPoint();
        } else {
            updateWorldPosition();
        }
    }
}, { deep: true });

// Watch settings to automatically start/stop GPS tracking
watch(() => settingsStore.settings.general.gpsEnabled, (enabled) => {
    if (enabled) {
        startGPSTracking();
    } else {
        stopGPSTracking();
    }
}, { immediate: true });

// Start tracking GPS position
function startGPSTracking() {
    if (!isSupported.value) {
        settingsStore.settings.general.gpsEnabled = false;
        return false;
    }

    gpsWorldPosition.isActive = true;
    resume();

    // Request permission
    if (navigator.permissions && navigator.permissions.query) {
        navigator.permissions.query({ name: 'geolocation' })
            .then(result => {
                if (result.state === 'denied') {
                    // If permission is denied, update UI accordingly
                    settingsStore.settings.general.gpsEnabled = false;
                    console.error('Geolocation permission denied');
                }
            })
            .catch(err => console.error('Permission query error:', err));
    }

    return true;
}

// Stop tracking GPS position
function stopGPSTracking() {
    pause();
    gpsWorldPosition.isActive = false;
}

// Get tracking status
function getGPSStatus() {
    return {
        isActive: gpsWorldPosition.isActive,
        isSupported: isSupported.value,
        coords: coords.value,
        error: error.value,
        origin: originPoint,
        worldOrigin: WORLD_ORIGIN,
        worldPosition: gpsWorldPosition,
        lastUpdate: locatedAt.value
    };
}

export {
    coords,
    error,
    originPoint,
    gpsWorldPosition,
    isSupported,
    startGPSTracking,
    stopGPSTracking,
    setOriginPoint,
    resetOriginPoint,
    getGPSStatus
};