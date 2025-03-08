import { reactive, ref } from 'vue';
import * as THREE from 'three';

// Player position data
const positionData = reactive({ x: 0, y: 0, z: 0 });
const position = new THREE.Vector3(0, 0, 0);

// Animation state
const isMoving = ref(false);
let currentAnimation = null;

// Camera reference
let cameraInstance = null;

// Constants
const INSTANT_TELEPORT_THRESHOLD = 1000; // 1km in world units

/**
 * Set the camera instance reference
 * @param {THREE.Camera} camera - The camera instance
 */
function setCameraInstance(camera) {
    cameraInstance = camera;
}

/**
 * Remove all controls and event listeners
 */
function removeControls() {
    if (currentAnimation) {
        cancelAnimationFrame(currentAnimation);
        currentAnimation = null;
    }
}

/**
 * Initialize player controls
 */
function setupControls() {
    // No initialization needed
}

/**
 * Helper to update position values
 */
function updatePosition(x, y, z) {
    positionData.x = x;
    positionData.y = y;
    positionData.z = z;
    position.set(x, y, z);
}

/**
 * Move player smoothly to a new position with horizontal movement only
 */
function moveTo(targetX, targetY, targetZ, duration = 500) {
    // Cancel any existing animation
    if (currentAnimation) {
        cancelAnimationFrame(currentAnimation);
    }

    // Keep y position unchanged if not specified
    targetY = targetY !== undefined ? targetY : positionData.y;

    // Calculate distance
    const distance = Math.sqrt(
        Math.pow(targetX - positionData.x, 2) +
        Math.pow(targetZ - positionData.z, 2)
    );

    // If distance is very small, skip animation
    if (distance < 0.001) {
        return Promise.resolve();
    }

    // If distance is greater than threshold (1km), teleport instantly
    if (distance > INSTANT_TELEPORT_THRESHOLD) {
        updatePosition(targetX, targetY, targetZ);
        return Promise.resolve();
    }

    // Set animation state
    isMoving.value = true;
    const startTime = performance.now();

    // Store start position
    const startPos = {
        x: positionData.x,
        y: targetY, // Use target Y (flat movement)
        z: positionData.z
    };

    const endPos = { x: targetX, y: targetY, z: targetZ };

    const animate = (currentTime) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);

        if (progress >= 1) {
            // Animation complete - set final position exactly
            updatePosition(endPos.x, endPos.y, endPos.z);

            isMoving.value = false;
            currentAnimation = null;
            return;
        }

        // Apply easing function for smooth acceleration/deceleration
        const easedT = progress * progress * (3 - 2 * progress); // Smoothstep easing

        // Calculate point along the path - horizontal movement only
        const newX = startPos.x + (endPos.x - startPos.x) * easedT;
        const newZ = startPos.z + (endPos.z - startPos.z) * easedT;

        // Update position (keeping y constant)
        updatePosition(newX, targetY, newZ);

        // Continue animation
        currentAnimation = requestAnimationFrame(animate);
    };

    // Start animation
    currentAnimation = requestAnimationFrame(animate);

    // Return promise that resolves when animation is complete
    return new Promise(resolve => {
        setTimeout(resolve, duration);
    });
}

/**
 * Teleport player to a new position with smooth animation or instantly if far
 */
function teleport(options) {
    const { x, z, y = positionData.y, duration = 500 } = options;
    return moveTo(x, y, z, duration);
}

// Export functions
export {
    position,
    positionData,
    setupControls,
    removeControls,
    setCameraInstance,
    moveTo,
    teleport,
    isMoving
};