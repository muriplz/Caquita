import { reactive } from 'vue';
import * as THREE from 'three';

// Player position data - will always be 0,0 since world moves instead
const positionData = reactive({ x: 0, y: 0, z: 0 });
const position = new THREE.Vector3(0, 0, 0);

// Camera reference
let cameraInstance = null;

/**
 * Set the camera instance reference
 * @param {THREE.Camera} camera - The camera instance
 */
function setCameraInstance(camera) {
    cameraInstance = camera;
}

/**
 * Remove all controls and event listeners
 * Called when component is unmounted
 */
function removeControls() {
    // No controls to remove
}

/**
 * Initialize player controls
 * Called when component is mounted
 */
function setupControls() {
    // Player is static at 0,0,0 - no controls needed
}

// Export what's needed
export {
    position,
    positionData,
    setupControls,
    removeControls,
    setCameraInstance
};