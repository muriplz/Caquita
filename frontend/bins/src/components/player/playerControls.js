import {reactive} from 'vue';
import * as THREE from 'three';

const positionData = reactive({ x: 0, y: 0, z: 0 });
const position = new THREE.Vector3(0, 0, 0);
const velocity = reactive(new THREE.Vector3(0, 0, 0));
const acceleration = 0.01;
const maxSpeed = 0.8;
const keys = {};

// Store camera direction
const cameraDirection = new THREE.Vector3();
const moveDirection = new THREE.Vector3();
let cameraInstance = null;
let isControlsActive = false;

function setCameraInstance(camera) {
    cameraInstance = camera;

    if (isControlsActive) {
        removeControls();
        setupControls();
    }
}

function handleKeyDown(event) {
    keys[event.key.toLowerCase()] = true;
}

function handleKeyUp(event) {
    keys[event.key.toLowerCase()] = false;
}

let animationFrameId = null;

function updatePosition() {
    if (!cameraInstance) {
        animationFrameId = requestAnimationFrame(updatePosition);
        return;
    }

    // Get camera's forward direction
    cameraDirection.set(0, 0, -1);
    cameraDirection.applyQuaternion(cameraInstance.quaternion);
    cameraDirection.y = 0;
    cameraDirection.normalize();

    // Calculate right vector
    const rightVector = new THREE.Vector3();
    rightVector.crossVectors(new THREE.Vector3(0, 1, 0), cameraDirection).normalize();

    // Reset move direction
    moveDirection.set(0, 0, 0);

    // Add movement based on keys
    if (keys['w']) {
        moveDirection.add(cameraDirection);
    }
    if (keys['s']) {
        moveDirection.sub(cameraDirection);
    }
    if (keys['a']) {
        moveDirection.add(rightVector);
    }
    if (keys['d']) {
        moveDirection.sub(rightVector);
    }

    // Apply movement
    if (moveDirection.lengthSq() > 0) {
        moveDirection.normalize();
        velocity.x = THREE.MathUtils.lerp(velocity.x, moveDirection.x * maxSpeed, 0.05);
        velocity.z = THREE.MathUtils.lerp(velocity.z, moveDirection.z * maxSpeed, 0.05);
    } else {
        velocity.x = THREE.MathUtils.lerp(velocity.x, 0, 0.05);
        velocity.z = THREE.MathUtils.lerp(velocity.z, 0, 0.05);
    }

    // Update position
    positionData.x += velocity.x;
    positionData.z += velocity.z;
    position.copy(positionData);

    animationFrameId = requestAnimationFrame(updatePosition);
}

function setupControls() {
    if (!isControlsActive) {
        window.addEventListener('keydown', handleKeyDown);
        window.addEventListener('keyup', handleKeyUp);
        isControlsActive = true;
        updatePosition();
    }
}

function removeControls() {
    window.removeEventListener('keydown', handleKeyDown);
    window.removeEventListener('keyup', handleKeyUp);
    isControlsActive = false;
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
    }
}

export {
    position,
    positionData,
    setupControls,
    removeControls,
    setCameraInstance
};