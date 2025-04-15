<template>
  <div class="map-preview" :class="{ 'fullscreen': isFullscreen }">
    <div class="map-container" ref="mapContainer">
      <div class="tiles-container">
        <div
            v-for="(tile, index) in visibleTiles"
            :key="index"
            class="map-tile"
            :style="getTileStyle(tile)">
        </div>
      </div>

      <div
          v-if="playerPosition"
          class="player-marker"
          :style="getPlayerMarkerStyle()">
      </div>

      <div
          v-if="selectedPosition"
          class="marker"
          :style="getMarkerStyle()"
          @mousedown.stop="startDrag('marker', $event)"
          @touchstart.stop="startTouchDrag('marker', $event)">
      </div>
    </div>

    <div v-if="isFullscreen && selectedPosition" class="position-info-container">
      <div class="position-info">
        {{ selectedPosition.lat }}, {{ selectedPosition.lng }}
      </div>
      <button class="select-position-button" @click="toggleFullscreen">
        Select Position
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { getCoordinates } from '@/js/map/Coordinates.js';
import { latLonToTile, tileToLatLon } from '@/js/map/TileConversion.js';
import { isTracking, getGPSStatus } from '@/components/player/GPSTracker.js';

const props = defineProps({
  initialLat: Number,
  initialLng: Number
});

const emit = defineEmits(['update:isFullscreen', 'positionSelected']);

// Map state
const isFullscreen = ref(false);
const mapContainer = ref(null);
const zoomLevel = ref(17);
const basePosition = ref({ lat: 0, lng: 0 });
const panOffset = ref({ x: 0, y: 0 });
const zoom = ref(1);
const visibleTiles = ref([]);
const tileSize = 256;

// Marker state
const selectedPosition = ref(null);
const playerPosition = ref(null);
const previewCenterOffset = ref({ x: 0, y: 0 });

// Computed property to determine the center position for preview mode
const previewCenter = computed(() => {
  if (selectedPosition.value) {
    return {
      lat: parseFloat(selectedPosition.value.lat),
      lng: parseFloat(selectedPosition.value.lng)
    };
  } else if (playerPosition.value) {
    return {
      lat: parseFloat(playerPosition.value.lat),
      lng: parseFloat(playerPosition.value.lng)
    };
  } else {
    return basePosition.value;
  }
});

// Interaction state
const dragState = ref({
  isDragging: false,
  type: null,
  startX: 0,
  startY: 0,
  lastX: 0,
  lastY: 0,
  pinchDistance: 0
});

// Load the initial 9 tiles - called ONCE
function loadInitialTiles() {
  if (!basePosition.value.lat) return;

  const centerTile = latLonToTile(basePosition.value.lat, basePosition.value.lng, zoomLevel.value);
  const visibleRadius = 1; // 3x3 grid (9 tiles)
  const newTiles = [];

  for (let y = -visibleRadius; y <= visibleRadius; y++) {
    for (let x = -visibleRadius; x <= visibleRadius; x++) {
      const tileX = centerTile.x + x;
      const tileY = centerTile.y + y;
      newTiles.push({
        tileX,
        tileY,
        url: `https://tiles.stadiamaps.com/tiles/stamen_toner_lite/${zoomLevel.value}/${tileX}/${tileY}.png`
      });
    }
  }
  visibleTiles.value = newTiles;
}

// Calculate preview offset needed to center on marker/player
function calculatePreviewOffset() {
  if (!isFullscreen.value && previewCenter.value && basePosition.value.lat) {
    // Calculate the offset needed to center on the marker or player
    const centerTile = latLonToTile(basePosition.value.lat, basePosition.value.lng, zoomLevel.value);
    const targetTile = latLonToTile(previewCenter.value.lat, previewCenter.value.lng, zoomLevel.value);

    // Calculate tile difference
    const tileDiffX = targetTile.x - centerTile.x;
    const tileDiffY = targetTile.y - centerTile.y;

    // Calculate position within tile
    const tileNW = tileToLatLon(targetTile.x, targetTile.y, zoomLevel.value);
    const tileSE = tileToLatLon(targetTile.x + 1, targetTile.y + 1, zoomLevel.value);

    const xRatio = (previewCenter.value.lng - tileNW.lon) / (tileSE.lon - tileNW.lon);
    const yRatio = (tileNW.lat - previewCenter.value.lat) / (tileNW.lat - tileSE.lat);

    // Calculate pixel offset to center on target
    const offsetX = -((tileDiffX + xRatio) * tileSize);
    const offsetY = -((tileDiffY + yRatio) * tileSize);

    previewCenterOffset.value = { x: offsetX, y: offsetY };
  } else {
    previewCenterOffset.value = { x: 0, y: 0 };
  }
}

// Get tile positioning style
function getTileStyle(tile) {
  if (!basePosition.value.lat) return {};

  const centerTile = latLonToTile(basePosition.value.lat, basePosition.value.lng, zoomLevel.value);
  const containerWidth = mapContainer.value?.clientWidth || 0;
  const containerHeight = mapContainer.value?.clientHeight || 0;
  const centerX = containerWidth / 2;
  const centerY = containerHeight / 2;

  // Apply pan offset in fullscreen mode, or preview offset in preview mode
  const offsetX = isFullscreen.value ? panOffset.value.x : previewCenterOffset.value.x;
  const offsetY = isFullscreen.value ? panOffset.value.y : previewCenterOffset.value.y;

  const x = centerX + ((tile.tileX - centerTile.x) * tileSize * zoom.value) + offsetX;
  const y = centerY + ((tile.tileY - centerTile.y) * tileSize * zoom.value) + offsetY;

  return {
    left: `${x}px`,
    top: `${y}px`,
    width: `${tileSize * zoom.value}px`,
    height: `${tileSize * zoom.value}px`,
    backgroundImage: `url(${tile.url})`
  };
}

// Calculate lat/lng from tile coordinates and within-tile ratios
function tileToPosition(tileX, tileY, xRatio, yRatio) {
  const nw = tileToLatLon(tileX, tileY, zoomLevel.value);
  const se = tileToLatLon(tileX + 1, tileY + 1, zoomLevel.value);

  const lng = nw.lon + (se.lon - nw.lon) * xRatio;
  const lat = nw.lat - (nw.lat - se.lat) * yRatio;

  return { lat, lng };
}

// Get marker positioning style - fixed to map
function getMarkerStyle() {
  if (!selectedPosition.value || !basePosition.value.lat) {
    return { display: 'none' };
  }

  const lat = parseFloat(selectedPosition.value.lat);
  const lng = parseFloat(selectedPosition.value.lng);

  return positionToStyle(lat, lng);
}

// Get player marker positioning style - fixed to map
function getPlayerMarkerStyle() {
  if (!playerPosition.value || !basePosition.value.lat) {
    return { display: 'none' };
  }

  const lat = parseFloat(playerPosition.value.lat);
  const lng = parseFloat(playerPosition.value.lng);

  return positionToStyle(lat, lng);
}

// Convert lat/lng to screen position with pan and zoom
function positionToStyle(lat, lng) {
  const centerTile = latLonToTile(basePosition.value.lat, basePosition.value.lng, zoomLevel.value);
  const markerTile = latLonToTile(lat, lng, zoomLevel.value);

  const containerWidth = mapContainer.value?.clientWidth || 0;
  const containerHeight = mapContainer.value?.clientHeight || 0;
  const centerX = containerWidth / 2;
  const centerY = containerHeight / 2;

  // Calculate distance in tiles
  const tileDiffX = markerTile.x - centerTile.x;
  const tileDiffY = markerTile.y - centerTile.y;

  // Calculate position within tile
  const tileNW = tileToLatLon(markerTile.x, markerTile.y, zoomLevel.value);
  const tileSE = tileToLatLon(markerTile.x + 1, markerTile.y + 1, zoomLevel.value);

  const xRatio = (lng - tileNW.lon) / (tileSE.lon - tileNW.lon);
  const yRatio = (tileNW.lat - lat) / (tileNW.lat - tileSE.lat);

  // Use appropriate offset based on mode
  const offsetX = isFullscreen.value ? panOffset.value.x : previewCenterOffset.value.x;
  const offsetY = isFullscreen.value ? panOffset.value.y : previewCenterOffset.value.y;

  // Apply tile position, within-tile position, zoom, and offset
  const x = centerX + ((tileDiffX + xRatio) * tileSize * zoom.value) + offsetX;
  const y = centerY + ((tileDiffY + yRatio) * tileSize * zoom.value) + offsetY;

  return {
    left: `${x}px`,
    top: `${y}px`,
    transform: 'translate(-50%, -50%)'
  };
}

// Convert screen position to lat/lng - accounts for pan and zoom
function screenToLatLng(screenX, screenY) {
  if (!basePosition.value.lat || !mapContainer.value) return null;

  const containerRect = mapContainer.value.getBoundingClientRect();
  const containerX = screenX - containerRect.left;
  const containerY = screenY - containerRect.top;

  const centerX = containerRect.width / 2;
  const centerY = containerRect.height / 2;

  // Use appropriate offset based on mode
  const offsetX = isFullscreen.value ? panOffset.value.x : previewCenterOffset.value.x;
  const offsetY = isFullscreen.value ? panOffset.value.y : previewCenterOffset.value.y;

  // Calculate position in tiles relative to center
  const tileDiffX = (containerX - centerX - offsetX) / (tileSize * zoom.value);
  const tileDiffY = (containerY - centerY - offsetY) / (tileSize * zoom.value);

  // Get base tile
  const centerTile = latLonToTile(basePosition.value.lat, basePosition.value.lng, zoomLevel.value);

  // Calculate target tile and position within tile
  const targetTileX = centerTile.x + Math.floor(tileDiffX);
  const targetTileY = centerTile.y + Math.floor(tileDiffY);

  // Calculate position within the tile (0-1 range)
  const xRatio = (tileDiffX % 1 + 1) % 1;
  const yRatio = (tileDiffY % 1 + 1) % 1;

  // Convert to lat/lng
  return tileToPosition(targetTileX, targetTileY, xRatio, yRatio);
}

// Mouse drag handling
function startDrag(type, e) {
  if (!isFullscreen.value) return;

  dragState.value = {
    isDragging: true,
    type,
    startX: e.clientX,
    startY: e.clientY,
    lastX: e.clientX,
    lastY: e.clientY,
    pinchDistance: 0
  };

  document.addEventListener('mousemove', handleDragMove);
  document.addEventListener('mouseup', handleDragEnd);

  e.preventDefault();
}

// Touch drag handling
function startTouchDrag(type, e) {
  if (!isFullscreen.value) return;

  const touch = e.touches[0];

  dragState.value = {
    isDragging: true,
    type,
    startX: touch.clientX,
    startY: touch.clientY,
    lastX: touch.clientX,
    lastY: touch.clientY,
    pinchDistance: e.touches.length === 2 ? calculateTouchDistance(e.touches) : 0
  };

  document.addEventListener('touchmove', handleTouchMove, { passive: true });
  document.addEventListener('touchend', handleTouchEnd, { passive: true });
}

function calculateTouchDistance(touches) {
  if (touches.length < 2) return 0;
  return Math.hypot(
      touches[1].clientX - touches[0].clientX,
      touches[1].clientY - touches[0].clientY
  );
}

// Handle mouse movement during drag
function handleDragMove(e) {
  if (!dragState.value.isDragging) return;

  const dx = e.clientX - dragState.value.lastX;
  const dy = e.clientY - dragState.value.lastY;

  if (dragState.value.type === 'map') {
    // Update visual position only
    panOffset.value.x += dx;
    panOffset.value.y += dy;
  } else if (dragState.value.type === 'marker') {
    updateMarkerPosition(e.clientX, e.clientY);
  }

  dragState.value.lastX = e.clientX;
  dragState.value.lastY = e.clientY;
}

// Handle touch movement during drag
function handleTouchMove(e) {
  if (!dragState.value.isDragging) return;

  // Handle pinch-to-zoom with two fingers
  if (e.touches.length === 2 && dragState.value.type === 'map') {
    const newDistance = calculateTouchDistance(e.touches);

    if (dragState.value.pinchDistance > 0) {
      const zoomFactor = newDistance / dragState.value.pinchDistance;
      zoom.value = Math.min(6, Math.max(0.1, zoom.value * zoomFactor));
    }

    dragState.value.pinchDistance = newDistance;
    return;
  }

  const touch = e.touches[0];
  const dx = touch.clientX - dragState.value.lastX;
  const dy = touch.clientY - dragState.value.lastY;

  if (dragState.value.type === 'map') {
    // Update visual position only
    panOffset.value.x += dx;
    panOffset.value.y += dy;
  } else if (dragState.value.type === 'marker') {
    updateMarkerPosition(touch.clientX, touch.clientY);
  }

  dragState.value.lastX = touch.clientX;
  dragState.value.lastY = touch.clientY;
}

// Update marker position based on screen coordinates
function updateMarkerPosition(screenX, screenY) {
  const latLng = screenToLatLng(screenX, screenY);
  if (!latLng) return;

  selectedPosition.value = {
    lat: latLng.lat.toFixed(6),
    lng: latLng.lng.toFixed(6)
  };
}

// Handle end of mouse drag
function handleDragEnd(e) {
  if (!dragState.value.isDragging) return;

  if (dragState.value.type === 'map') {
    const totalMovement = Math.hypot(
        e.clientX - dragState.value.startX,
        e.clientY - dragState.value.startY
    );

    // If minimal movement, treat as a click to set marker
    if (totalMovement < 5) {
      updateMarkerPosition(e.clientX, e.clientY);
      emit('positionSelected', selectedPosition.value);
    }
  } else if (dragState.value.type === 'marker') {
    emit('positionSelected', selectedPosition.value);
  }

  document.removeEventListener('mousemove', handleDragMove);
  document.removeEventListener('mouseup', handleDragEnd);

  dragState.value.isDragging = false;
}

// Handle end of touch drag
function handleTouchEnd(e) {
  if (!dragState.value.isDragging) return;

  if (dragState.value.type === 'map' && e.changedTouches.length === 1) {
    const touch = e.changedTouches[0];
    const totalMovement = Math.hypot(
        touch.clientX - dragState.value.startX,
        touch.clientY - dragState.value.startY
    );

    // If minimal movement, treat as a tap to set marker
    if (totalMovement < 5) {
      updateMarkerPosition(touch.clientX, touch.clientY);
      emit('positionSelected', selectedPosition.value);
    }
  } else if (dragState.value.type === 'marker') {
    emit('positionSelected', selectedPosition.value);
  }

  document.removeEventListener('touchmove', handleTouchMove);
  document.removeEventListener('touchend', handleTouchEnd);

  dragState.value.isDragging = false;
  dragState.value.pinchDistance = 0;
}

// Handle map clicks directly
function handleMapClick(e) {
  if (!isFullscreen.value || dragState.value.isDragging) return;

  const target = e.target;
  if (target.classList.contains('marker') || target.classList.contains('player-marker')) return;

  updateMarkerPosition(e.clientX, e.clientY);
  emit('positionSelected', selectedPosition.value);
}

// Handle mouse wheel for zooming
function handleWheel(e) {
  if (!isFullscreen.value) return;

  const oldZoom = zoom.value;

  if (e.deltaY < 0) {
    zoom.value = Math.min(6, zoom.value * 1.1);
  } else {
    zoom.value = Math.max(0.1, zoom.value * 0.9);
  }
}

// Toggle fullscreen mode
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value;
  emit('update:isFullscreen', isFullscreen.value);

  // Reset zoom and pan
  zoom.value = 1;
  panOffset.value = { x: 0, y: 0 };

  nextTick(() => {
    // When exiting fullscreen, recalculate the preview offset
    if (!isFullscreen.value) {
      calculatePreviewOffset();
    }
    // Only load tiles if going into fullscreen
    else if (isFullscreen.value) {
      loadInitialTiles();
    }
  });
}

// Public method to set fullscreen state
function setFullscreen(value) {
  isFullscreen.value = value;
  emit('update:isFullscreen', value);

  // Reset zoom and pan
  zoom.value = 1;
  panOffset.value = { x: 0, y: 0 };

  nextTick(() => {
    // When exiting fullscreen, recalculate the preview offset
    if (!isFullscreen.value) {
      calculatePreviewOffset();
    }
    // Only load tiles if going into fullscreen
    else if (isFullscreen.value) {
      loadInitialTiles();
    }
  });
}

// Handle escape key to exit fullscreen
function handleEscKey(e) {
  if (e.key === 'Escape' && isFullscreen.value) {
    toggleFullscreen();
  }
}

// Watch for changes to update the UI
watch(selectedPosition, (newVal) => {
  if (newVal) {
    emit('positionSelected', newVal);

    // Update preview centering when marker changes and not in fullscreen
    if (!isFullscreen.value) {
      calculatePreviewOffset();
    }
  }
});

watch(previewCenter, () => {
  if (!isFullscreen.value) {
    calculatePreviewOffset();
  }
});

watch(isFullscreen, (newVal) => {
  document.body.style.overflow = newVal ? 'hidden' : '';
  document.documentElement.style.overflow = newVal ? 'hidden' : '';

  if (newVal) {
    window.addEventListener('keydown', handleEscKey);
  } else {
    window.removeEventListener('keydown', handleEscKey);
  }
});

// Initialize component
onMounted(() => {
  // Set up event listeners
  mapContainer.value.addEventListener('click', handleMapClick);
  mapContainer.value.addEventListener('wheel', handleWheel, { passive: true });
  mapContainer.value.addEventListener('mousedown', (e) => {
    if (!e.target.classList.contains('marker')) {
      startDrag('map', e);
    }
  });
  mapContainer.value.addEventListener('touchstart', (e) => {
    if (!e.target.classList.contains('marker')) {
      startTouchDrag('map', e);
    }
  }, { passive: true });

  window.addEventListener('resize', () => {
    // Recalculate preview offset when resizing in preview mode
    if (!isFullscreen.value) {
      calculatePreviewOffset();
    }
    // Only re-render tiles if in fullscreen mode
    else if (isFullscreen.value) {
      nextTick(() => loadInitialTiles());
    }
  });

  // Initialize map center
  let coords;
  if (isTracking.value) {
    const gpsStatus = getGPSStatus();
    if (gpsStatus.firstPosition) {
      coords = {
        lat: gpsStatus.firstPosition.lat,
        lon: gpsStatus.firstPosition.lon
      };
    } else {
      coords = getCoordinates();
    }
  } else if (props.initialLat && props.initialLng) {
    coords = {lat: props.initialLat, lon: props.initialLng};
  } else {
    coords = getCoordinates();
  }

  basePosition.value = {
    lat: coords.lat,
    lng: coords.lon
  };

  // Set initial marker position if provided
  if (props.initialLat && props.initialLng) {
    selectedPosition.value = {
      lat: props.initialLat.toFixed(6),
      lng: props.initialLng.toFixed(6)
    };
  }

  // Set player position marker
  playerPosition.value = {
    lat: coords.lat,
    lng: coords.lon
  };

  // Load initial tiles only once
  nextTick(() => {
    loadInitialTiles();
    // Calculate preview offset after tiles are loaded
    calculatePreviewOffset();
  });
});

defineExpose({ setFullscreen });
</script>

<style scoped>
.map-preview {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: default;
  border: 1px solid #ddd;
}

.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  border-radius: 0;
  background: white;
}

.map-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
}

.tiles-container {
  position: absolute;
  width: 100%;
  height: 100%;
}

.map-tile {
  position: absolute;
  background-size: cover;
  background-repeat: no-repeat;
}

.marker {
  position: absolute;
  width: 28px;
  height: 28px;
  background-color: red;
  border: 2px solid white;
  border-radius: 50%;
  z-index: 10;
  cursor: grab;
}

.player-marker {
  position: absolute;
  width: 28px;
  height: 28px;
  background-color: blue;
  border: 2px solid white;
  border-radius: 50%;
  z-index: 9;
}

.position-info-container {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.position-info {
  background-color: rgba(241, 159, 56, 0.8);
  padding: 8px 12px;
  border-radius: 4px;
  text-align: center;
}

.select-position-button {
  background-color: #da7019;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-weight: bold;
  font: inherit;
}
</style>