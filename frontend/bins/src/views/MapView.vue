<script setup>
import {TresCanvas} from '@tresjs/core'
import OrbitControls from "../components/player/OrbitControls.vue"
import {Sky, Stats} from '@tresjs/cientos'
import PlayerEntity from "../components/player/PlayerEntity.vue"
import TileRenderer from "../components/TileRenderer.vue"
import {onMounted, ref, onUnmounted} from 'vue'
import InfinitePlane from "../components/InfinitePlane.vue"
import { WORLD_ORIGIN } from "@/js/map/TileConversion.js"
import { positionData } from "../components/player/playerControls.js"
import { isTracking, getGPSStatus } from "../components/player/GPSTracker.js"

const showDebugInfo = ref(false)
const gpsInfo = ref({})
const updateInterval = ref(null)

// Function to update GPS info display
function updateGPSInfo() {
  gpsInfo.value = getGPSStatus()
}

onMounted(() => {
  // Add keyboard shortcut to toggle debug info
  window.addEventListener('keydown', (e) => {
    if (e.key === 'F3') {
      showDebugInfo.value = !showDebugInfo.value
    }
  })

  // Update GPS info every second
  updateInterval.value = setInterval(updateGPSInfo, 1000)
})

onUnmounted(() => {
  if (updateInterval.value) {
    clearInterval(updateInterval.value)
  }
})
</script>

<template>
  <div class="map-container">
    <TresCanvas window-size>
      <Sky/>
      <Stats v-if="showDebugInfo"/>
      <TresPerspectiveCamera :args="[45, 1, 0.1, 1000]"/>
      <PlayerEntity/>
      <OrbitControls/>
      <TileRenderer />
      <InfinitePlane/>
      <TresDirectionalLight :position="[10, 10, 10]" :intensity="0.8" cast-shadow/>
    </TresCanvas>

    <!-- Always visible GPS Debug Panel -->
    <div v-if="showDebugInfo" class="gps-debug-panel">
      <h3>GPS Debug Info</h3>

      <div class="debug-section">
        <h4>World Origin</h4>
        <p>
          <span>Lat: {{ WORLD_ORIGIN.lat.toFixed(6) }}</span>
          <span>Lon: {{ WORLD_ORIGIN.lon.toFixed(6) }}</span>
          <span>Custom: {{ WORLD_ORIGIN.isCustom ? 'Yes' : 'No' }}</span>
        </p>
      </div>

      <div class="debug-section">
        <h4>Player Position</h4>
        <p>
          <span>X: {{ positionData.x.toFixed(2) }}</span>
          <span>Z: {{ positionData.z.toFixed(2) }}</span>
        </p>
      </div>

      <div class="debug-section">
        <h4>GPS Status</h4>
        <p>
          <span>Tracking: {{ isTracking ? 'Active' : 'Inactive' }}</span>
          <span>Updates: {{ gpsInfo.updateCount || 0 }}</span>
        </p>
        <div v-if="gpsInfo.firstPosition">
          <p>
            <span>First GPS Position:</span>
          </p>
          <p>
            <span>Lat: {{ gpsInfo.firstPosition.lat.toFixed(6) }}</span>
            <span>Lon: {{ gpsInfo.firstPosition.lon.toFixed(6) }}</span>
          </p>
        </div>
      </div>
    </div>

    <div v-if="!showDebugInfo" class="debug-overlay">
      <div class="debug-info">
        <p>F3: Show Debug Info</p>
        <p>F4: Toggle tile debug</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.debug-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
  z-index: 1000;
}

.debug-info p {
  margin: 5px 0;
  font-size: 14px;
}

.gps-debug-panel {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px;
  border-radius: 5px;
  width: 220px;
  font-family: monospace;
  z-index: 1000;
}

.gps-debug-panel h3 {
  margin-top: 0;
  margin-bottom: 10px;
  text-align: center;
  font-size: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 5px;
}

.debug-section {
  margin-bottom: 10px;
}

.debug-section h4 {
  margin: 5px 0;
  font-size: 14px;
  color: #8af;
}

.debug-section p {
  margin: 5px 0;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.debug-section span {
  display: inline-block;
}
</style>