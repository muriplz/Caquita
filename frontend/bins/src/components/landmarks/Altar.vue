<script setup>

import {positionData} from "@/components/player/playerControls.js";
import {Cylinder} from "@tresjs/cientos";
import {ref} from "vue";

const color = ref('gray')

</script>

<template>
  <TresMesh :position="[positionData.x+10, positionData.y , positionData.z+10]">
    <Cylinder :args="[2, 2, 0.3, 32]">
      <TresMeshToonMaterial :color="color"/>
    </Cylinder>

    <Cylinder :args="[1.5, 1.5, 0.6, 32]">
      <TresMeshToonMaterial :color="color"/>
    </Cylinder>

    <slot></slot>
  </TresMesh>
</template>

<style scoped>

</style>