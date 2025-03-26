<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import maplibregl from 'maplibre-gl';
import { getCoordinates } from '@/js/map/coordinates.js';

const mapContainer = ref(null);
const map = ref(null);
const marker = ref(null);
const selectedPosition = ref(null);
const isDragging = ref(false);

onMounted(() => {
  initializeMap();
});

onUnmounted(() => {
  if (map.value) {
    if (marker.value) marker.value.remove();
    map.value.remove();
  }
});

function initializeMap() {
  if (!mapContainer.value) return;

  // Get initial position from coordinates.js
  const coordinates = getCoordinates();

  // Create the map
  map.value = new maplibregl.Map({
    container: mapContainer.value,
    style: '/jsons/map_styles/caquita.json',
    center: [coordinates.lon, coordinates.lat],
    zoom: 16,
    maxZoom: 16,
    minZoom: 16,
    attributionControl: false,
    boxZoom: false,
    dragRotate: false,
    touchZoomRotate: false,
    maxBounds: [
      [coordinates.lon - 0.01, coordinates.lat - 0.01], // SW bounds (approximately 500m)
      [coordinates.lon + 0.01, coordinates.lat + 0.01]  // NE bounds (approximately 500m)
    ]
  });

  // Create marker element
  const markerElement = document.createElement('div');
  markerElement.className = 'position-marker';

  // Wait for map to load before adding interactions
  map.value.on('load', () => {
    // Add click handler
    map.value.on('click', handleMapClick);

    // Create but don't add marker yet
    marker.value = new maplibregl.Marker(markerElement);
  });
}

function handleMapClick(e) {
  if (isDragging.value) return;

  // Get clicked coordinates
  const lngLat = e.lngLat;

  // Update selected position
  selectedPosition.value = {
    lat: lngLat.lat.toFixed(6),
    lng: lngLat.lng.toFixed(6)
  };

  // Update marker position
  if (marker.value) {
    marker.value.setLngLat(lngLat).addTo(map.value);
  }
}
</script>

<template>
  <div class="top-down-map-wrapper">
    <div ref="mapContainer" class="map-container"></div>

    <div v-if="selectedPosition" class="selected-position">
      Selected: {{ selectedPosition.lat }}, {{ selectedPosition.lng }}
    </div>
  </div>
</template>

<style scoped>
.top-down-map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.selected-position {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 8px;
  border-radius: 4px;
  font-size: 14px;
  pointer-events: none;
}

:global(.position-marker) {
  width: 14px;
  height: 14px;
  background-color: red;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 0 3px rgba(0, 0, 0, 0.5);
}
</style>