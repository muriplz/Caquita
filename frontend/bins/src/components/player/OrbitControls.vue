<script setup>
import {BaseCameraControls, CameraControls} from '@tresjs/cientos'
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import {positionData, removeControls, setCameraInstance} from './playerControls.js'
import settingsManager from '@/components/ui/settings/settings.js'
import * as THREE from 'three'

// Constants
const MIN_DISTANCE = 15
const MAX_DISTANCE = 70
const DEAD_ZONE_RADIUS = 20
const MIN_POLAR_ANGLE = Math.PI * (25 / 180)
const MAX_POLAR_ANGLE = Math.PI * (87 / 180)
const HEAD_OFFSET = 1.6

// State variables
const controlsRef = ref(null)
const isDragging = ref(false)
const lastAngle = ref(0)
const currentDistance = ref(50)

// Computed properties
const cameraSensitivityX = computed(() => settingsManager.settings.controls.cameraSensitivityX * 1.1 / 5)
const zoomSpeed = computed(() => settingsManager.settings.controls.zoomSpeed * 3)

// Helper function to calculate polar angle based on distance
const calculatePolarAngleFromDistance = (distance) => {
  const normalizedDistance = (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)
  const easedDistance = normalizedDistance * normalizedDistance
  return MAX_POLAR_ANGLE - easedDistance * (MAX_POLAR_ANGLE - MIN_POLAR_ANGLE)
}

// Zoom camera to a specific distance
const updateCameraZoom = (newDistance) => {
  if (!controlsRef.value?.instance) return

  try {
    // Clamp distance within limits
    newDistance = Math.min(MAX_DISTANCE, Math.max(MIN_DISTANCE, newDistance))

    // Apply zoom
    const control = controlsRef.value.instance
    control.distance = newDistance
    control.polarAngle = calculatePolarAngleFromDistance(newDistance)
    control.update()

    // Update state
    currentDistance.value = newDistance
  } catch (error) {
    console.error("Zoom error:", error)
  }
}

// Initialize camera controls
const onReady = (instance) => {
  if (!instance) return

  controlsRef.value = instance

  try {
    // Set up camera parameters
    if (instance._camera) {
      instance._camera.near = 0.1
      instance._camera.far = 1000
      instance._camera.updateProjectionMatrix()
    }

    // Initialize target to player position
    instance.setTarget(
        positionData.x,
        positionData.y + HEAD_OFFSET,
        positionData.z
    )

    // Set initial distance and angle
    instance.distance = currentDistance.value
    instance.polarAngle = calculatePolarAngleFromDistance(currentDistance.value)
    instance.update()

    // Store camera reference for external use
    if (instance._camera) {
      setCameraInstance(instance._camera)
    }
  } catch (error) {
    console.error("Camera initialization error:", error)
  }
}

// Clean up on component unmount
onUnmounted(() => {
  removeControls()
})

// Set up interaction handling
onMounted(() => {
  const canvas = document.querySelector('canvas')
  if (!canvas) return

  // Helper function to get angle from center
  const getAngleFromCenter = (x, y) => {
    const rect = canvas.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2
    return Math.atan2(y - centerY, x - centerX)
  }

  // Helper function to get distance from center
  const getDistanceFromCenter = (x, y) => {
    const rect = canvas.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2
    const dx = x - centerX
    const dy = y - centerY
    return Math.sqrt(dx * dx + dy * dy)
  }

  // Handle interaction start
  const handleStart = (x, y) => {
    if (getDistanceFromCenter(x, y) > DEAD_ZONE_RADIUS) {
      isDragging.value = true
      lastAngle.value = getAngleFromCenter(x, y)
    }
  }

  // Handle mouse/touch move
  const handleMove = (x, y) => {
    if (!isDragging.value || !controlsRef.value?.instance) return

    // Check if outside dead zone
    const distanceFromCenter = getDistanceFromCenter(x, y)
    if (distanceFromCenter <= DEAD_ZONE_RADIUS) return

    // Calculate angle change
    const currentAngle = getAngleFromCenter(x, y)
    let deltaAngle = currentAngle - lastAngle.value

    // Normalize angle delta
    if (deltaAngle > Math.PI) deltaAngle -= Math.PI * 2
    if (deltaAngle < -Math.PI) deltaAngle += Math.PI * 2

    // Apply rotation
    const cameraControl = controlsRef.value.instance
    const distanceScale = Math.min(1, (distanceFromCenter - DEAD_ZONE_RADIUS) / DEAD_ZONE_RADIUS)

    cameraControl.azimuthAngle += deltaAngle * cameraSensitivityX.value * distanceScale
    cameraControl.update()

    // Update state
    lastAngle.value = currentAngle
    currentDistance.value = cameraControl.distance
  }

  // Handle interaction end
  const handleEnd = () => {
    isDragging.value = false
    if (controlsRef.value?.instance) {
      currentDistance.value = controlsRef.value.instance.distance
    }
  }

  // Mouse event handlers
  canvas.addEventListener('mousedown', (e) => handleStart(e.clientX, e.clientY))
  canvas.addEventListener('mousemove', (e) => handleMove(e.clientX, e.clientY))
  canvas.addEventListener('mouseup', handleEnd)
  canvas.addEventListener('mouseleave', handleEnd)

  // Touch event handlers
  canvas.addEventListener('touchstart', (e) => {
    e.preventDefault()
    if (e.touches.length === 1) {
      const touch = e.touches[0]
      handleStart(touch.clientX, touch.clientY)
    }
  }, {passive: false})

  canvas.addEventListener('touchmove', (e) => {
    e.preventDefault()
    if (e.touches.length === 1) {
      const touch = e.touches[0]
      handleMove(touch.clientX, touch.clientY)
    }
  }, {passive: false})

  canvas.addEventListener('touchend', handleEnd)
  canvas.addEventListener('touchcancel', handleEnd)

  // Handle pinch zoom
  let lastTouchDistance = 0

  canvas.addEventListener('touchstart', (e) => {
    if (e.touches.length === 2) {
      const dx = e.touches[0].clientX - e.touches[1].clientX
      const dy = e.touches[0].clientY - e.touches[1].clientY
      lastTouchDistance = Math.sqrt(dx * dx + dy * dy)
    }
  })

  canvas.addEventListener('touchmove', (e) => {
    if (e.touches.length === 2 && controlsRef.value?.instance) {
      const dx = e.touches[0].clientX - e.touches[1].clientX
      const dy = e.touches[0].clientY - e.touches[1].clientY
      const distance = Math.sqrt(dx * dx + dy * dy)

      const delta = lastTouchDistance - distance
      const newDistance = currentDistance.value + (delta * 0.01 * zoomSpeed.value)

      updateCameraZoom(newDistance)
      lastTouchDistance = distance
    }
  })

  // Handle wheel zoom
  canvas.addEventListener('wheel', (e) => {
    if (!controlsRef.value?.instance) return
    e.preventDefault()

    const zoomChange = zoomSpeed.value
    const newDistance = e.deltaY > 0
        ? Math.min(currentDistance.value + zoomChange, MAX_DISTANCE)
        : Math.max(currentDistance.value - zoomChange, MIN_DISTANCE)

    updateCameraZoom(newDistance)
  }, {passive: false})

  // Initialize controls if they're already available
  if (controlsRef.value?.instance) {
    onReady(controlsRef.value.instance)
  }
})

// Watch for player position changes to update camera target
watch(positionData, (newPosition) => {
  if (!controlsRef.value?.instance) return

  try {
    // Save current camera state
    const control = controlsRef.value.instance
    const currentAzimuth = control.azimuthAngle
    const currentPolar = control.polarAngle
    const currentDist = control.distance

    // Update target (the point camera looks at)
    control.setTarget(
        newPosition.x,
        newPosition.y + HEAD_OFFSET,
        newPosition.z,
        false // Don't update yet
    )

    // Explicitly maintain camera's relative position
    control.azimuthAngle = currentAzimuth
    control.polarAngle = currentPolar
    control.distance = currentDist

    // Apply all changes
    control.update()
  } catch (error) {
    console.error("Position update error:", error)
  }
}, {deep: true})
</script>

<template>
  <CameraControls
      ref="controlsRef"
      @ready="onReady"
      :min-distance="MIN_DISTANCE"
      :max-distance="MAX_DISTANCE"
      :min-polar-angle="MIN_POLAR_ANGLE"
      :max-polar-angle="MAX_POLAR_ANGLE"
      :truck-speed="0"
      :azimuth-rotate-speed="0"
      :polar-rotate-speed="0"
      :smooth-time="0"
      :drag-to-offset="false"
      :mouse-buttons="{
      left: BaseCameraControls.ACTION.NONE,
      right: BaseCameraControls.ACTION.NONE,
      middle: BaseCameraControls.ACTION.NONE,
      wheel: BaseCameraControls.ACTION.NONE
    }"
      make-default
  />
</template>