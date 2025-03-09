<script setup>
import {TresCanvas} from '@tresjs/core'
import OrbitControls from "../components/player/OrbitControls.vue"
import {Sky, Stats} from '@tresjs/cientos'
import PlayerEntity from "../components/player/PlayerEntity.vue"
import {onMounted, onUnmounted, ref} from 'vue'
import InfinitePlane from "../components/InfinitePlane.vue"
import {getGPSStatus} from "../components/player/GPSTracker.js"
import MapCanvas from "@/components/MapCanvas.vue";

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
      <TresPerspectiveCamera :args="[25, 1, 0.1, 1000]"/>
      <PlayerEntity/>
      <OrbitControls/>
      <MapCanvas />
      <InfinitePlane/>
      <TresDirectionalLight :position="[10, 10, 10]" :intensity="0.8" cast-shadow/>
    </TresCanvas>
  </div>
</template>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>