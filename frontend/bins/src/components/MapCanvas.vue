<script setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue';
import {mapLibreManager} from '../js/map/MapLibreManager.js';
import {positionData} from './player/playerControls.js';
import {TILE_SIZE, worldToLatLon} from '../js/map/TileConversion.js';
import {getGPSStatus, isTracking} from './player/GPSTracker.js';
import * as THREE from 'three';
import {tileLoader} from "@/js/map/TileLoader.js";

const mapTexture = ref(null);
const material = ref(null);
const canvasSize = 1024;
const debugMode = ref(false);

let isComponentMounted = false;
let mapContainer = null;
let rafId = null;
let lastUpdateTime = 0;
let lastPosition = { x: 0, z: 0 };
let needsUpdate = false;

const centerCoordinates = computed(() => {
  const gpsStatus = getGPSStatus();

  if (isTracking.value && gpsStatus.firstPosition) {
    return {
      lat: gpsStatus.firstPosition.lat,
      lon: gpsStatus.firstPosition.lon,
      isGps: true
    };
  }

  const { lat, lon } = worldToLatLon(positionData.x, positionData.z);
  return { lat, lon, isGps: false };
});

const worldSize = computed(() => {
  const dist = tileLoader.renderDistance;
  return (2 * dist + 1) * TILE_SIZE;
});

onMounted(async () => {
  isComponentMounted = true;

  mapContainer = document.createElement('div');
  mapContainer.style.position = 'absolute';
  mapContainer.style.width = `${canvasSize}px`;
  mapContainer.style.height = `${canvasSize}px`;
  mapContainer.style.visibility = 'hidden';
  document.body.appendChild(mapContainer);

  try {
    await mapLibreManager.initialize(mapContainer);
    await createMapTexture();
    updateMapCenter();

    const updateLoop = () => {
      if (!isComponentMounted) return;

      // Check if player moved significantly
      checkPositionChange();

      // Only update texture when needed
      if (needsUpdate) {
        updateMapTexture();
        needsUpdate = false;
      }

      rafId = requestAnimationFrame(updateLoop);
    };

    rafId = requestAnimationFrame(updateLoop);
  } catch (error) {
    console.error('Failed to initialize map:', error);
  }
});

onUnmounted(() => {
  isComponentMounted = false;

  if (rafId) {
    cancelAnimationFrame(rafId);
    rafId = null;
  }

  mapLibreManager.dispose();

  if (mapContainer) {
    document.body.removeChild(mapContainer);
  }

  if (mapTexture.value) {
    mapTexture.value.dispose();
  }

  if (material.value) {
    material.value.dispose();
  }
});

watch(centerCoordinates, () => {
  if (isComponentMounted) {
    updateMapCenter();
    needsUpdate = true;
  }
}, { immediate: false });

watch(isTracking, () => {
  if (isComponentMounted) {
    updateMapCenter();
    needsUpdate = true;
  }
}, { immediate: false });

function checkPositionChange() {
  // Only trigger updates when player has moved significantly
  const dx = Math.abs(positionData.x - lastPosition.x);
  const dz = Math.abs(positionData.z - lastPosition.z);

  if (dx > 1 || dz > 1) {
    lastPosition.x = positionData.x;
    lastPosition.z = positionData.z;
    updateMapCenter();
    needsUpdate = true;
  }
}

function updateMapCenter() {
  const center = centerCoordinates.value;
  mapLibreManager.setCenter(center.lat, center.lon);
}

async function createMapTexture() {
  const canvas = await mapLibreManager.getMapTexture();

  if (!canvas) return;

  mapTexture.value = new THREE.CanvasTexture(canvas);
  mapTexture.value.wrapS = THREE.RepeatWrapping;
  mapTexture.value.wrapT = THREE.RepeatWrapping;

  mapTexture.value.colorSpace = THREE.SRGBColorSpace;

  // Optimize texture settings
  mapTexture.value.magFilter = THREE.NearestFilter;
  mapTexture.value.minFilter = THREE.NearestFilter;
  mapTexture.value.generateMipmaps = false;
  mapTexture.value.anisotropy = 1;

  material.value = new THREE.MeshBasicMaterial({
    map: mapTexture.value,
    transparent: false,
    opacity: 1.0
  });
}

async function updateMapTexture() {
  if (!mapTexture.value || !material.value) return;

  // Throttle updates to avoid excessive redraws
  const now = performance.now();
  if (now - lastUpdateTime < 100) return;

  lastUpdateTime = now;
  mapTexture.value.needsUpdate = true;
}

function getMaterial() {
  if (!material.value) {
    material.value = new THREE.MeshBasicMaterial({
      color: 0xffffff,
      transparent: false,
      opacity: 1.0
    });
  }
  return material.value;
}
</script>

<template>
  <TresMesh
      :position="[0, 0, 0]"
      :rotation="[-Math.PI / 2, 0, 0]"
  >
    <TresPlaneGeometry :args="[worldSize, worldSize]" />
    <primitive :object="getMaterial()" />

    <TresMesh v-if="debugMode" :position="[0, 0.01, 0]">
      <TresPlaneGeometry :args="[worldSize - 1, worldSize - 1]"/>
      <TresMeshBasicMaterial wireframe color="red" transparent :opacity="0.5"/>
    </TresMesh>
  </TresMesh>
</template>