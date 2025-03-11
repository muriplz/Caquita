<script setup>
import { onMounted, onUnmounted, ref, watch, computed } from 'vue';
import { mapLibreManager } from '../js/map/MapLibreManager.js';
import { positionData } from './player/playerControls.js';
import { worldToLatLon, latLonToWorld, TILE_SIZE } from '../js/map/TileConversion.js';
import { getGPSStatus, isTracking } from './player/GPSTracker.js';
import * as THREE from 'three';
import settingsManager from '@/components/ui/settings/settings.js';

const mapTexture = ref(null);
const material = ref(null);
const canvasSize = 2048;
const debugMode = ref(false);

let isComponentMounted = false;
let mapContainer = null;
let updateTimer = null;

const renderDistance = computed(() => {
  return settingsManager.settings.graphics.renderDistance;
});

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
  const dist = renderDistance.value;
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
    createMapTexture();
    updateMapCenter();
    updateTimer = setInterval(updateMapTexture, 250);
  } catch (error) {
    console.error('Failed to initialize map:', error);
  }

  const toggleDebug = (e) => {
    if (e.key === 'F4') {
      debugMode.value = !debugMode.value;
    }
  };

  window.addEventListener('keydown', toggleDebug);
});

onUnmounted(() => {
  isComponentMounted = false;

  window.removeEventListener('keydown', toggleDebug);

  if (updateTimer) {
    clearInterval(updateTimer);
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
  }
}, { immediate: false });

watch(renderDistance, () => {
  if (isComponentMounted) {
    updateMapCenter();
    updateMapTexture();
  }
}, { immediate: false });

watch(isTracking, () => {
  if (isComponentMounted) {
    updateMapCenter();
  }
}, { immediate: false });

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

  mapTexture.value.encoding = THREE.sRGBEncoding;

  material.value = new THREE.MeshBasicMaterial({
    map: mapTexture.value,
    transparent: true
  });
}

async function updateMapTexture() {
  if (!mapTexture.value || !material.value) return;
  mapTexture.value.needsUpdate = true;
}

function getMaterial() {
  if (!material.value) {
    material.value = new THREE.MeshBasicMaterial({
      color: 0xffffff, // Change from 0xcccccc to white
      transparent: false, // Remove transparency
      opacity: 1.0 // Full opacity
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