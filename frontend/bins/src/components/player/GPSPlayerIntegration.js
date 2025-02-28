import { watch } from 'vue';
import { gpsWorldPosition } from './GPSTracker.js';
import { position, positionData } from './playerControls.js';

let isGPSControlActive = false;

function enableGPSControl() {
    isGPSControlActive = true;
}

function disableGPSControl() {
    isGPSControlActive = false;
}

function toggleGPSControl() {
    isGPSControlActive = !isGPSControlActive;
    return isGPSControlActive;
}

function getGPSControlStatus() {
    return isGPSControlActive;
}

watch(gpsWorldPosition, (newPos) => {
    if (isGPSControlActive && gpsWorldPosition.isActive) {
        positionData.x = newPos.x;
        positionData.z = newPos.z;

        position.set(newPos.x, positionData.y, newPos.z);
    }
}, { deep: true });

export {
    enableGPSControl,
    disableGPSControl,
    toggleGPSControl,
    getGPSControlStatus
};