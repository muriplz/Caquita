<script setup>
import {positionData} from "@/components/player/playerControls.js";
import {Cylinder} from "@tresjs/cientos";
import {ref} from "vue";

const emit = defineEmits(['canClick']);

const altarPosition = [positionData.x+10, positionData.y, positionData.z+10]
const mainColor = ref("#444444")

const handleTrashClick = (event) => {
  console.log('Can clicked!', event);
  console.log('Click coordinates:', event.clientX, event.clientY);

  emit('canClick', {
    x: event.clientX || window.innerWidth / 2,
    y: event.clientY || window.innerHeight / 2
  });

  mainColor.value = "#888888";
  setTimeout(() => {
    mainColor.value = "#444444";
  }, 300);
}
</script>

<template>
  <TresGroup :position="[altarPosition[0], altarPosition[1] + 0.7, altarPosition[2]]">
    <TresMesh @click="handleTrashClick">
      <Cylinder :args="[1.2, 1.2, 2, 16]">
        <TresMeshBasicMaterial :color="mainColor" transparent :opacity="0.3" />
      </Cylinder>
    </TresMesh>

    <TresMesh>
      <Cylinder :args="[0.7, 0.6, 1.2, 16]">
        <TresMeshBasicMaterial :color="mainColor" />
      </Cylinder>
    </TresMesh>

    <TresMesh :position="[0, 0.65, 0]">
      <Cylinder :args="[0.75, 0.75, 0.1, 16]">
        <TresMeshBasicMaterial color="#333333" />
      </Cylinder>
    </TresMesh>

    <TresMesh :position="[0.7, 0, 0]">
      <TresBoxGeometry :args="[0.1, 0.5, 0.1]" />
      <TresMeshBasicMaterial color="#555555" />
    </TresMesh>

    <TresMesh :position="[-0.7, 0, 0]">
      <TresBoxGeometry :args="[0.1, 0.5, 0.1]" />
      <TresMeshBasicMaterial color="#555555" />
    </TresMesh>
  </TresGroup>
</template>