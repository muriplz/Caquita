<script setup>
import {BaseCameraControls, CameraControls} from '@tresjs/cientos'
import {onMounted, onUnmounted, reactive, ref, watch} from 'vue'
import {positionData, removeControls, setCameraInstance} from './playerControls.js'
import * as THREE from 'three'

const DRAG_SENSITIVITY = 1.1
const MIN_DISTANCE = 2
const MAX_DISTANCE = 70
const ZOOM_SPEED = 3
const DEAD_ZONE_RADIUS = 20
const MIN_POLAR_ANGLE = Math.PI * (25 / 180)
const MAX_POLAR_ANGLE = Math.PI * (87 / 180)

const isDragging = ref(false)
const lastAngle = ref(0)
const lastY = ref(0)
const currentDistance = ref(10)

const controlsRef = ref(null)
const offsetPos = reactive({x: 0, y: 0, z: 0})
const targetPosition = new THREE.Vector3()

const updateCameraPosition = (position, offset, cameraControls, distance) => {
  if (!cameraControls) return

  targetPosition.set(
      position.x,
      position.y,
      position.z
  )

  const desiredX = position.x + offset.x
  const desiredY = position.y + offset.y
  const desiredZ = position.z + offset.z

  cameraControls.setLookAt(
      desiredX, desiredY, desiredZ,
      targetPosition.x, targetPosition.y, targetPosition.z,
      true
  )
}

const onReady = (instance) => {
  controlsRef.value = instance
  currentDistance.value = instance.distance

  currentDistance.value = 20
  
  if (instance) {
    instance.setTarget(positionData.x, positionData.y, positionData.z)
    instance.distance = currentDistance.value
  }

  if (instance?._camera) {
    setCameraInstance(instance._camera)
  }
}

onUnmounted(() => {
  removeControls()
})

onMounted(() => {
  const canvas = document.querySelector('canvas')
  if (!canvas) return

  const getAngleFromCenter = (x, y) => {
    const rect = canvas.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2
    return Math.atan2(y - centerY, x - centerX)
  }

  const getDistanceFromCenter = (x, y) => {
    const rect = canvas.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2
    const dx = x - centerX
    const dy = y - centerY
    return Math.sqrt(dx * dx + dy * dy)
  }

  const handleStart = (x, y) => {
    if (getDistanceFromCenter(x, y) > DEAD_ZONE_RADIUS) {
      isDragging.value = true
      lastAngle.value = getAngleFromCenter(x, y)
      lastY.value = y
    }
  }

  const handleMove = (x, y) => {
    if (!isDragging.value || !controlsRef.value?.instance) return

    const distanceFromCenter = getDistanceFromCenter(x, y)
    if (distanceFromCenter <= DEAD_ZONE_RADIUS) {
      return
    }

    const currentAngle = getAngleFromCenter(x, y)
    let deltaAngle = currentAngle - lastAngle.value

    if (deltaAngle > Math.PI) deltaAngle -= Math.PI * 2
    if (deltaAngle < -Math.PI) deltaAngle += Math.PI * 2

    const cameraControl = controlsRef.value.instance

    const distanceScale = Math.min(1, (distanceFromCenter - DEAD_ZONE_RADIUS) / DEAD_ZONE_RADIUS)

    if (cameraControl.azimuthAngle !== undefined) {
      cameraControl.azimuthAngle += deltaAngle * DRAG_SENSITIVITY * distanceScale
    }

    const deltaY = (y - lastY.value) * 0.005 * distanceScale
    if (cameraControl.polarAngle !== undefined) {
      const newPolarAngle = Math.min(MAX_POLAR_ANGLE,
          Math.max(MIN_POLAR_ANGLE,
              cameraControl.polarAngle - deltaY))
      cameraControl.polarAngle = newPolarAngle
    }
    setOffset()
    cameraControl.update()

    lastAngle.value = currentAngle
    lastY.value = y
    currentDistance.value = cameraControl.distance
  }

  const handleEnd = () => {
    isDragging.value = false
    if (controlsRef.value?.instance) {
      currentDistance.value = controlsRef.value.instance.distance
    }
  }

  canvas.addEventListener('mousedown', (e) => {
    handleStart(e.clientX, e.clientY)
  })

  canvas.addEventListener('mousemove', (e) => {
    handleMove(e.clientX, e.clientY)
  })

  canvas.addEventListener('mouseup', handleEnd)
  canvas.addEventListener('mouseleave', handleEnd)

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
      const cameraControl = controlsRef.value.instance
      let newDistance = cameraControl.distance + (delta * 0.01)

      newDistance = Math.min(MAX_DISTANCE, Math.max(MIN_DISTANCE, newDistance))
      cameraControl.distance = newDistance
      cameraControl.update()

      lastTouchDistance = distance
      currentDistance.value = newDistance
    }
  })

  canvas.addEventListener('wheel', (e) => {
    if (!controlsRef.value?.instance) return

    e.preventDefault()

    const cameraControl = controlsRef.value.instance
    let distance = cameraControl.distance

    if (e.deltaY > 0) {
      distance = Math.min(distance + ZOOM_SPEED, MAX_DISTANCE)
    } else {
      distance = Math.max(distance - ZOOM_SPEED, MIN_DISTANCE)
    }

    cameraControl.distance = distance
    cameraControl.update()
    currentDistance.value = distance
  }, {passive: false})

  onReady(controlsRef.value?.instance)

  if (controlsRef.value?.instance?._camera) {
    setCameraInstance(controlsRef.value.instance._camera)
  }
})

const setOffset = () => {
  const target = controlsRef.value.instance._target
  const cameraPosition = controlsRef.value.instance._camera.position

  offsetPos.x = (!isNaN(target.x) && !isNaN(cameraPosition.x)) ? cameraPosition.x - target.x : 0
  offsetPos.y = (!isNaN(target.y) && !isNaN(cameraPosition.y)) ? cameraPosition.y - target.y : 0
  offsetPos.z = (!isNaN(target.z) && !isNaN(cameraPosition.z)) ? cameraPosition.z - target.z : 0
}

watch(positionData, (newPosition) => {
  setOffset()
  updateCameraPosition(newPosition, offsetPos, controlsRef.value.instance, currentDistance.value)
  if (controlsRef.value?.instance) {
    controlsRef.value.instance.setTarget(newPosition.x, newPosition.y, newPosition.z)
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