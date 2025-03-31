<template>
  <div class="map-preview" :class="{ 'fullscreen': isFullscreen }">

    <div class="map-container" ref="mapContainer">
      <!-- Static tiles container - never scaled -->
      <div class="tiles-container">
        <div
            v-for="(tile, index) in visibleTiles"
            :key="index"
            class="map-tile"
            :style="getTileStyle(tile)">
        </div>
      </div>

      <!-- Marker - positioned independently based on lat/lng -->
      <div
          v-if="selectedPosition"
          class="marker"
          :style="getMarkerStyle()"
          @mousedown.stop="startMarkerDrag"
          @touchstart.stop="startMarkerTouchDrag">
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

// Core state
const isFullscreen = ref(false);
const selectedPosition = ref(null);
const mapContainer = ref(null);
const zoomLevel = ref(17);
const centerPosition = ref({ lat: 0, lng: 0 });
const panOffset = ref({ x: 0, y: 0 });
const zoom = ref(1);
const visibleTiles = ref([]);
const tileSize = 256;
const isDragging = ref(false);
const isMarkerDragging = ref(false);
const dragStart = ref({ x: 0, y: 0 });
const lastTouch = ref({ x: 0, y: 0 });
const lastDistance = ref(0);

function calculateVisibleTiles() {
  if (!centerPosition.value.lat) return;

  const centerTile = latLonToTile(centerPosition.value.lat, centerPosition.value.lng, zoomLevel.value);

  // Limit tiles to a fixed grid (5x5)
  const visibleRadius = 2;

  // Create a new array to hold visible tiles
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

  // Replace tiles with the new set
  visibleTiles.value = newTiles;
}


// Calculate tile position in the container
function getTileStyle(tile) {
  if (!centerPosition.value.lat) return {};

  const centerTile = latLonToTile(centerPosition.value.lat, centerPosition.value.lng, zoomLevel.value);

  // Calculate the center of the viewport
  const containerWidth = mapContainer.value ? mapContainer.value.clientWidth : 0;
  const containerHeight = mapContainer.value ? mapContainer.value.clientHeight : 0;
  const centerX = containerWidth / 2;
  const centerY = containerHeight / 2;

  // Calculate position based on distance from center tile
  const x = centerX + ((tile.tileX - centerTile.x) * tileSize * zoom.value) + panOffset.value.x;
  const y = centerY + ((tile.tileY - centerTile.y) * tileSize * zoom.value) + panOffset.value.y;

  return {
    left: `${x}px`,
    top: `${y}px`,
    width: `${tileSize * zoom.value}px`,
    height: `${tileSize * zoom.value}px`,
    backgroundImage: `url(${tile.url})`
  };
}

// Calculate marker position based on lat/lng
function getMarkerStyle() {
  if (!selectedPosition.value || !centerPosition.value.lat) {
    return { display: 'none' };
  }

  const lat = parseFloat(selectedPosition.value.lat);
  const lng = parseFloat(selectedPosition.value.lng);
  const centerTile = latLonToTile(centerPosition.value.lat, centerPosition.value.lng, zoomLevel.value);
  const markerTile = latLonToTile(lat, lng, zoomLevel.value);

  // Calculate the center of the viewport
  const containerWidth = mapContainer.value ? mapContainer.value.clientWidth : 0;
  const containerHeight = mapContainer.value ? mapContainer.value.clientHeight : 0;
  const centerX = containerWidth / 2;
  const centerY = containerHeight / 2;

  // Calculate tile position difference from center
  const tileDiffX = markerTile.x - centerTile.x;
  const tileDiffY = markerTile.y - centerTile.y;

  // Calculate position within tile
  const centerTileBounds = {
    nw: tileToLatLon(markerTile.x, markerTile.y, zoomLevel.value),
    se: tileToLatLon(markerTile.x + 1, markerTile.y + 1, zoomLevel.value)
  };

  const xRatio = (lng - centerTileBounds.nw.lon) / (centerTileBounds.se.lon - centerTileBounds.nw.lon);
  const yRatio = (centerTileBounds.nw.lat - lat) / (centerTileBounds.nw.lat - centerTileBounds.se.lat);

  // Calculate final position
  const x = centerX + ((tileDiffX + xRatio) * tileSize * zoom.value) + panOffset.value.x;
  const y = centerY + ((tileDiffY + yRatio) * tileSize * zoom.value) + panOffset.value.y;

  return {
    left: `${x}px`,
    top: `${y}px`,
    transform: 'translate(-50%, -50%)'
  };
}

// Convert screen position to lat/lng
function screenToLatLng(screenX, screenY) {
  if (!centerPosition.value.lat || !mapContainer.value) return null;

  const containerRect = mapContainer.value.getBoundingClientRect();
  const containerX = screenX - containerRect.left;
  const containerY = screenY - containerRect.top;

  // Calculate the center of the viewport
  const centerX = containerRect.width / 2;
  const centerY = containerRect.height / 2;

  // Calculate difference from center in tile units
  const tileDiffX = (containerX - centerX - panOffset.value.x) / (tileSize * zoom.value);
  const tileDiffY = (containerY - centerY - panOffset.value.y) / (tileSize * zoom.value);

  // Get center tile
  const centerTile = latLonToTile(centerPosition.value.lat, centerPosition.value.lng, zoomLevel.value);

  // Calculate target tile and position within tile
  const targetTileX = centerTile.x + Math.floor(tileDiffX);
  const targetTileY = centerTile.y + Math.floor(tileDiffY);

  const xRatio = (tileDiffX % 1 + 1) % 1;
  const yRatio = (tileDiffY % 1 + 1) % 1;

  // Get tile bounds
  const tileBounds = {
    nw: tileToLatLon(targetTileX, targetTileY, zoomLevel.value),
    se: tileToLatLon(targetTileX + 1, targetTileY + 1, zoomLevel.value)
  };

  // Calculate lat/lng
  const lng = tileBounds.nw.lon + (tileBounds.se.lon - tileBounds.nw.lon) * xRatio;
  const lat = tileBounds.nw.lat - (tileBounds.nw.lat - tileBounds.se.lat) * yRatio;

  return { lat, lng };
}

watch(selectedPosition, (newVal) => {
  if (newVal) emit('positionSelected', newVal);
});

watch([centerPosition, zoom], () => {
  calculateVisibleTiles();
});

watch(isFullscreen, (newVal) => {
  document.body.style.overflow = newVal ? 'hidden' : '';
  document.documentElement.style.overflow = newVal ? 'hidden' : '';
  if (newVal) {
    window.addEventListener('keydown', handleEscKey);
  } else {
    window.removeEventListener('keydown', handleEscKey);
  }

  // Recalculate visible tiles after fullscreen change
  nextTick(() => calculateVisibleTiles());
});

onMounted(() => {
  window.addEventListener('resize', handleResize);

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

  centerPosition.value = {
    lat: coords.lat,
    lng: coords.lon
  };

  // Set initial position if provided
  if (props.initialLat && props.initialLng) {
    selectedPosition.value = {
      lat: props.initialLat.toFixed(6),
      lng: props.initialLng.toFixed(6)
    };
  }

  nextTick(() => calculateVisibleTiles());
});

function handleResize() {
  calculateVisibleTiles();
}

function handleEscKey(e) {
  if (e.key === 'Escape' && isFullscreen.value) {
    toggleFullscreen();
  }
}

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value;
  emit('update:isFullscreen', isFullscreen.value);

  // Reset zoom and pan
  zoom.value = 1;
  panOffset.value = { x: 0, y: 0 };

  // Wait for DOM update then recalculate
  nextTick(() => calculateVisibleTiles());
}

function setFullscreen(value) {
  isFullscreen.value = value;
  emit('update:isFullscreen', value);

  // Reset zoom and pan
  zoom.value = 1;
  panOffset.value = { x: 0, y: 0 };

  // Wait for DOM update then recalculate
  nextTick(() => calculateVisibleTiles());
}

// Map clicking to set marker
function handleMapClick(e) {
  if (!isFullscreen.value || isMarkerDragging.value || isDragging.value) return;

  // Calculate lat/lng from click position
  const latLng = screenToLatLng(e.clientX, e.clientY);
  if (!latLng) return;

  // Update marker position
  selectedPosition.value = {
    lat: latLng.lat.toFixed(6),
    lng: latLng.lng.toFixed(6)
  };

  emit('positionSelected', selectedPosition.value);
}

// Zooming with mouse wheel
function handleWheel(e) {
  if (!isFullscreen.value) return;

  // Calculate new zoom level
  const oldZoom = zoom.value;
  if (e.deltaY < 0) {
    zoom.value = Math.min(6, zoom.value * 1.1);
  } else {
    zoom.value = Math.max(0.1, zoom.value * 0.9);
  }

  calculateVisibleTiles();
}

// Start map dragging
function handleMouseDown(e) {
  if (!isFullscreen.value || isMarkerDragging.value) return;

  // Check if we're clicking the marker
  const target = e.target;
  if (target.classList.contains('marker')) return;

  // Start map dragging
  isDragging.value = true;
  dragStart.value = { x: e.clientX, y: e.clientY };

  document.addEventListener('mousemove', handleMouseMove);
  document.addEventListener('mouseup', handleMouseUp);

  e.preventDefault();
}

function handleMouseMove(e) {
  if (!isDragging.value) return;

  const dx = e.clientX - dragStart.value.x;
  const dy = e.clientY - dragStart.value.y;

  // Just update the pan offset
  panOffset.value.x += dx;
  panOffset.value.y += dy;

  dragStart.value = { x: e.clientX, y: e.clientY };
}

// End map dragging
function handleMouseUp() {
  isDragging.value = false;
  document.removeEventListener('mousemove', handleMouseMove);
  document.removeEventListener('mouseup', handleMouseUp);
}

// Start marker dragging
function startMarkerDrag(e) {
  if (!isFullscreen.value) return;

  isMarkerDragging.value = true;
  dragStart.value = { x: e.clientX, y: e.clientY };

  document.addEventListener('mousemove', moveMarker);
  document.addEventListener('mouseup', endMarkerDrag);

  e.stopPropagation();
  e.preventDefault();
}

// Move marker while dragging
function moveMarker(e) {
  if (!isMarkerDragging.value) return;

  // Calculate lat/lng from current mouse position
  const latLng = screenToLatLng(e.clientX, e.clientY);
  if (!latLng) return;

  // Update marker position
  selectedPosition.value = {
    lat: latLng.lat.toFixed(6),
    lng: latLng.lng.toFixed(6)
  };

  e.stopPropagation();
  e.preventDefault();
}

function moveMap(dx, dy) {
  // Convert screen pixels to geographic distance
  const metersPerPixel = 156543.03392 * Math.cos(centerPosition.value.lat * Math.PI / 180) / Math.pow(2, zoomLevel.value);
  const adjustedMetersPerPixel = metersPerPixel / zoom.value;

  // 111,111 meters = 1 degree latitude
  const latChange = (dy * adjustedMetersPerPixel) / 111111;
  // 111,111 * cos(lat) meters = 1 degree longitude
  const lngChange = (dx * adjustedMetersPerPixel) / (111111 * Math.cos(centerPosition.value.lat * Math.PI / 180));

  // Update center position
  centerPosition.value = {
    lat: centerPosition.value.lat + latChange,
    lng: centerPosition.value.lng + lngChange
  };

  // Reset pan offset since we've applied it to the center position
  panOffset.value = { x: 0, y: 0 };
}

// End marker dragging
function endMarkerDrag(e) {
  document.removeEventListener('mousemove', moveMarker);
  document.removeEventListener('mouseup', endMarkerDrag);

  isMarkerDragging.value = false;
  emit('positionSelected', selectedPosition.value);

  e.stopPropagation();
  e.preventDefault();
}

// Start marker dragging on touch
function startMarkerTouchDrag(e) {
  if (!isFullscreen.value) return;

  isMarkerDragging.value = true;
  lastTouch.value = { x: e.touches[0].clientX, y: e.touches[0].clientY };

  document.addEventListener('touchmove', moveMarkerTouch, { passive: true });
  document.addEventListener('touchend', endMarkerTouchDrag, { passive: true });
}

// Move marker on touch drag
function moveMarkerTouch(e) {
  if (!isMarkerDragging.value) return;

  // Calculate lat/lng from current touch position
  const latLng = screenToLatLng(e.touches[0].clientX, e.touches[0].clientY);
  if (!latLng) return;

  // Update marker position
  selectedPosition.value = {
    lat: latLng.lat.toFixed(6),
    lng: latLng.lng.toFixed(6)
  };
}

// End marker touch dragging
function endMarkerTouchDrag() {
  document.removeEventListener('touchmove', moveMarkerTouch);
  document.removeEventListener('touchend', endMarkerTouchDrag);

  isMarkerDragging.value = false;
  emit('positionSelected', selectedPosition.value);
}

// When the map container is mounted, set up event listeners
onMounted(() => {
  if (mapContainer.value) {
    mapContainer.value.addEventListener('click', handleMapClick);
    mapContainer.value.addEventListener('wheel', handleWheel, { passive: true });
    mapContainer.value.addEventListener('mousedown', handleMouseDown);
    mapContainer.value.addEventListener('touchstart', handleTouchStart, { passive: true });
    mapContainer.value.addEventListener('touchmove', handleTouchMove, { passive: true });
    mapContainer.value.addEventListener('touchend', handleTouchEnd, { passive: true });
  }
});

// Handle touch events for map panning and zooming
function handleTouchStart(e) {
  if (!isFullscreen.value) return;

  // Check if touching marker
  if (e.target.classList.contains('marker')) return;

  // Handle pinch-to-zoom with two fingers
  if (e.touches.length === 2) {
    const touch1 = e.touches[0];
    const touch2 = e.touches[1];
    lastDistance.value = Math.hypot(
        touch2.clientX - touch1.clientX,
        touch2.clientY - touch1.clientY
    );
    return;
  }

  // Single touch for panning
  if (e.touches.length === 1) {
    isDragging.value = true;
    lastTouch.value = { x: e.touches[0].clientX, y: e.touches[0].clientY };
  }
}

// Replace these two functions to prevent tile loading during mobile drag

function handleTouchMove(e) {
  if (!isFullscreen.value) return;

  // Handle pinch-to-zoom
  if (e.touches.length === 2) {
    const touch1 = e.touches[0];
    const touch2 = e.touches[1];

    // Calculate new distance
    const newDistance = Math.hypot(
        touch2.clientX - touch1.clientX,
        touch2.clientY - touch1.clientY
    );

    // Skip if initial distance not set
    if (lastDistance.value === 0) {
      lastDistance.value = newDistance;
      return;
    }

    // Adjust zoom based on pinch
    const zoomFactor = newDistance / lastDistance.value;
    zoom.value = Math.min(6, Math.max(0.1, zoom.value * zoomFactor));

    lastDistance.value = newDistance;
    return; // Don't recalculate tiles here
  }

  // Handle panning with one finger
  if (e.touches.length === 1 && isDragging.value && !isMarkerDragging.value) {
    const dx = e.touches[0].clientX - lastTouch.value.x;
    const dy = e.touches[0].clientY - lastTouch.value.y;

    // ONLY update visual pan offset, don't trigger tile recalculation
    panOffset.value.x += dx;
    panOffset.value.y += dy;

    lastTouch.value = { x: e.touches[0].clientX, y: e.touches[0].clientY };
  }
}

function handleTouchEnd(e) {
  lastDistance.value = 0;

  if (isMarkerDragging.value) return;

  // DON'T update centerPosition or recalculate tiles
  // Just leave the visual pan offset as is

  // Handle tap to place marker
  if (isDragging.value && e.changedTouches.length === 1) {
    const moveDistance = Math.hypot(
        e.changedTouches[0].clientX - lastTouch.value.x,
        e.changedTouches[0].clientY - lastTouch.value.y
    );

    if (moveDistance < 5 && !e.target.classList.contains('marker')) {
      // It was a tap, calculate lat/lng from touch position
      const latLng = screenToLatLng(e.changedTouches[0].clientX, e.changedTouches[0].clientY);
      if (latLng) {
        // Update marker position
        selectedPosition.value = {
          lat: latLng.lat.toFixed(6),
          lng: latLng.lng.toFixed(6)
        };

        emit('positionSelected', selectedPosition.value);
      }
    }
  }

  isDragging.value = false;
}

defineExpose({setFullscreen});
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

.fullscreen-toggle {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 20;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
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