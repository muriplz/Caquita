<template>
  <div class="map-preview" :class="{ 'fullscreen': isFullscreen }">
    <button class="fullscreen-toggle" @click.stop="toggleFullscreen">
      <span v-if="!isFullscreen">Expand</span>
      <span v-else>Close</span>
    </button>

    <div class="map-container" @click="handleMapClick">
      <div class="tiles-container" :style="transformStyle" @mousedown="startDrag" @mouseup="stopDrag" @mousemove="drag">
        <div
            v-for="(tile, index) in tiles"
            :key="index"
            class="map-tile"
            :style="{
            left: `${tile.x * 256}px`,
            top: `${tile.y * 256}px`,
            backgroundImage: `url(${tile.url})`
          }">
        </div>
        <motion.div
            v-if="selectedPosition"
            class="marker"
            :style="{ left: `${markerPosition.x}px`, top: `${markerPosition.y}px` }"
            :animate="{ x: 0, y: 0 }"
            :transition="{ type: 'spring', stiffness: 300, damping: 30 }"
            drag
            :drag-momentum="false"
            @dragend="updateMarkerPosition"
            @click.stop
        ></motion.div>
      </div>
    </div>

    <div v-if="isFullscreen" class="controls">
      <div class="zoom-controls">
        <button @click.stop="zoom(0.2)">+</button>
        <button @click.stop="zoom(-0.2)">-</button>
      </div>
      <div v-if="selectedPosition" class="position-info">
        {{ selectedPosition.lat }}, {{ selectedPosition.lng }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { motion } from 'motion-v';
import { getCoordinates } from '@/js/map/Coordinates.js';
import { latLonToTile, tileToLatLon } from '@/js/map/TileConversion.js';

const props = defineProps({
  initialLat: Number,
  initialLng: Number
});

const emit = defineEmits(['update:isFullscreen', 'positionSelected']);

const isFullscreen = ref(false);
const selectedPosition = ref(null);
const zoomLevel = ref(1);
const tiles = ref([]);
const markerPosition = ref({ x: 0, y: 0 });
const isDragging = ref(false);
const dragStart = ref({ x: 0, y: 0 });
const mapOffset = ref({ x: 0, y: 0 });

const transformStyle = computed(() => ({
  transform: `scale(${zoomLevel.value}) translate(${mapOffset.value.x}px, ${mapOffset.value.y}px)`,
  transformOrigin: 'center center'
}));

onMounted(() => {
  const coords = props.initialLat && props.initialLng
      ? { lat: props.initialLat, lon: props.initialLng }
      : getCoordinates();

  const centerTile = latLonToTile(coords.lat, coords.lon, 16);

  for (let y = -1; y <= 1; y++) {
    for (let x = -1; x <= 1; x++) {
      const tileX = centerTile.x + x;
      const tileY = centerTile.y + y;

      tiles.value.push({
        x: x + 1,
        y: y + 1,
        tileX,
        tileY,
        url: `https://tiles.stadiamaps.com/tiles/osm_bright/16/${tileX}/${tileY}.png`
      });
    }
  }
});

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value;
  emit('update:isFullscreen', isFullscreen.value);
  document.body.style.overflow = isFullscreen.value ? 'hidden' : '';
}

function setFullscreen(value) {
  isFullscreen.value = value;
  emit('update:isFullscreen', value);
  document.body.style.overflow = value ? 'hidden' : '';
}

function startDrag(e) {
  if (!isFullscreen.value || e.target.classList.contains('marker')) return;

  isDragging.value = true;
  dragStart.value = {x: e.clientX, y: e.clientY};

  document.addEventListener('mousemove', drag);
  document.addEventListener('mouseup', stopDrag, {once: true});
}

function stopDrag() {
  isDragging.value = false;
  document.removeEventListener('mousemove', drag);
}

function drag(e) {
  if (!isDragging.value) return;

  const dx = (e.clientX - dragStart.value.x) / zoomLevel.value;
  const dy = (e.clientY - dragStart.value.y) / zoomLevel.value;

  mapOffset.value.x += dx;
  mapOffset.value.y += dy;

  dragStart.value.x = e.clientX;
  dragStart.value.y = e.clientY;
}

function zoom(delta) {
  if (!isFullscreen.value) return;
  zoomLevel.value = Math.max(0.5, Math.min(3, zoomLevel.value + delta));
}

function handleMapClick(e) {
  if (!isFullscreen.value || isDragging.value) return;

  const rect = e.currentTarget.getBoundingClientRect();
  const rawX = (e.clientX - rect.left) / zoomLevel.value - mapOffset.value.x;
  const rawY = (e.clientY - rect.top) / zoomLevel.value - mapOffset.value.y;

  updatePosition(rawX, rawY);
}

function updateMarkerPosition(e) {
  if (!selectedPosition.value) return;

  const rect = e.target.getBoundingClientRect();
  const mapRect = e.target.offsetParent.getBoundingClientRect();

  const rawX = (rect.left + rect.width / 2 - mapRect.left) / zoomLevel.value - mapOffset.value.x;
  const rawY = (rect.top + rect.height / 2 - mapRect.top) / zoomLevel.value - mapOffset.value.y;

  updatePosition(rawX, rawY);
}

function updatePosition(rawX, rawY) {
  const tileX = Math.floor(rawX / 256);
  const tileY = Math.floor(rawY / 256);

  const tile = tiles.value.find(t => t.x === tileX && t.y === tileY);
  if (!tile) return;

  const tileOffsetX = rawX % 256;
  const tileOffsetY = rawY % 256;

  const tileBounds = {
    nw: tileToLatLon(tile.tileX, tile.tileY, 16),
    se: tileToLatLon(tile.tileX + 1, tile.tileY + 1, 16)
  };

  const lng = tileBounds.nw.lon + (tileBounds.se.lon - tileBounds.nw.lon) * (tileOffsetX / 256);
  const lat = tileBounds.nw.lat + (tileBounds.se.lat - tileBounds.nw.lat) * (tileOffsetY / 256);

  selectedPosition.value = {
    lat: lat.toFixed(6),
    lng: lng.toFixed(6)
  };

  markerPosition.value = {x: rawX - 14, y: rawY - 14};
  emit('positionSelected', selectedPosition.value);
}

defineExpose({setFullscreen});
</script>

<style scoped>
.map-preview {
  width: 300px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: default;
}

.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 999;
  border-radius: 0;
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
}

.tiles-container {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.2s ease;
}

.map-tile {
  position: absolute;
  width: 256px;
  height: 256px;
  background-size: cover;
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

.controls {
  position: absolute;
  bottom: 20px;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
  pointer-events: none;
}

.position-info {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 8px 12px;
  border-radius: 4px;
  pointer-events: auto;
}

.zoom-controls {
  display: flex;
  flex-direction: column;
  pointer-events: auto;
}

.zoom-controls button {
  width: 40px;
  height: 40px;
  margin: 4px 0;
  cursor: pointer;
}
</style>