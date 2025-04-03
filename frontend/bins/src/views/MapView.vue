<script setup>
import {TresCanvas} from '@tresjs/core'
import OrbitControls from "../components/player/OrbitControls.vue"
import {Sky} from '@tresjs/cientos'
import PlayerEntity from "../components/player/PlayerEntity.vue"
import {onMounted, onUnmounted, ref} from 'vue'
import InfinitePlane from "../components/InfinitePlane.vue"
import {getGPSStatus} from "../components/player/GPSTracker.js"
import MapCanvas from "@/components/MapCanvas.vue";
import Footer from "@/components/Footer.vue";
import Can from "@/components/landmarks/Can.vue";
import CanUi from "@/components/landmarks/CanUi.vue";

const gpsInfo = ref({})
const updateInterval = ref(null)
const showCanUi = ref(false);
const clickPosition = ref({ x: 0, y: 0 });

function updateGPSInfo() {
  gpsInfo.value = getGPSStatus()
}

function handleCanClick(position) {
  console.log('Can click received in MapView!', position);
  clickPosition.value = position;
  showCanUi.value = true;
}

function closeCanUi() {
  showCanUi.value = false;
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
      <Can @canClick="handleCanClick"/>
      <OrbitControls/>
      <MapCanvas />
      <InfinitePlane/>
      <TresDirectionalLight :position="[10, 10, 10]" :intensity="0.8" cast-shadow/>
    </TresCanvas>
    <Footer />

    <CanUi :show="showCanUi" :clickPosition="clickPosition" @close="closeCanUi" />
  </div>
</template>