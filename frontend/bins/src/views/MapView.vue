<script setup>
import { TresCanvas } from '@tresjs/core'
import OrbitControls from "../components/player/OrbitControls.vue"
import { Sky } from '@tresjs/cientos'
import PlayerEntity from "../components/player/PlayerEntity.vue"
import { onMounted, onUnmounted, ref } from 'vue'
import InfinitePlane from "../components/InfinitePlane.vue"
import { getGPSStatus } from "../components/player/GPSTracker.js"
import MapCanvas from "@/components/MapCanvas.vue"
import Footer from "@/components/Footer.vue"
import TrashCans from "@/components/landmarks/TrashCans.vue"
import LandmarkBuilder from "@/components/landmarks/LandmarkBuilder.vue"

const gpsInfo = ref({})
const updateInterval = ref(null)

function updateGPSInfo() {
  gpsInfo.value = getGPSStatus()
}

onMounted(() => {
  updateInterval.value = setInterval(updateGPSInfo, 1000)
})

onUnmounted(() => {
  if (updateInterval.value) {
    clearInterval(updateInterval.value)
  }
})
</script>

<template>
  <div>
    <TresCanvas window-size>
      <Sky/>
      <TresPerspectiveCamera :args="[25, 1, 0.1, 1000]"/>
      <PlayerEntity/>
      <LandmarkBuilder>
        <TrashCans/>
      </LandmarkBuilder>
      <OrbitControls/>
      <MapCanvas/>
      <InfinitePlane/>
      <TresDirectionalLight :position="[10, 10, 10]" :intensity="0.8" cast-shadow/>
    </TresCanvas>
    <Footer/>
  </div>
</template>