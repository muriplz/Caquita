// src/views/MapView.vue
<script setup>
import {TresCanvas} from '@tresjs/core'
import OrbitControls from "../components/player/OrbitControls.vue"
import {Sky, Stats} from '@tresjs/cientos'
import PlayerEntity from "../components/player/PlayerEntity.vue"
import TileRenderer from "../components/TileRenderer.vue"
import {onMounted, ref} from 'vue'
import InfinitePlane from "../components/InfinitePlane.vue";
import Menu from "../components/ui/Menu.vue";

const showDebugInfo = ref(false)

onMounted(() => {
  // Add keyboard shortcut to toggle debug info
  window.addEventListener('keydown', (e) => {
    if (e.key === 'F3') {
      showDebugInfo.value = !showDebugInfo.value
    }
  })
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

    <div v-if="showDebugInfo" class="debug-overlay">
      <div class="debug-info">
        <p>Debug Mode: ON (F3)</p>
        <p>Controls: WASD to move</p>
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
</style>