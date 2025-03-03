import { reactive } from 'vue';
import * as THREE from 'three';

// Player position data
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
    // Nothing to clean up since we've removed WASD controls
}

/**
 * Initialize player controls
 * Called when component is mounted
 */
function setupControls() {
    // We're not using keyboard controls anymore,
    // But kept this function for API compatibility
}

// Export only what's needed
export {
    position,
    positionData,
    setupControls,
    removeControls,
    setCameraInstance
};