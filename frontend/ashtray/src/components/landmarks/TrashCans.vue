<script setup>
import { ref, inject, onMounted, onBeforeUnmount, computed } from "vue";
import { Cylinder } from "@tresjs/cientos";
import * as THREE from "three";
import { useTres } from "@tresjs/core";
import TrashCanStore from "@/js/landmarks/trash_cans/TrashCanStore.js";
import { latLonToWorld, TILE_SIZE } from "@/js/map/TileConversion.js";

const landmarkSystem = inject('landmarkSystem');
const mainColor = ref("#444444");
const canvasElement = ref(null);
const isInteracting = ref(false);
const { camera, renderer, scene } = useTres();

const safeTrashCans = computed(() => {
  const cans = TrashCanStore.getTrashCans();
  return Array.isArray(cans) ? cans : [];
});

function getPosition(trashCan) {
  if (!trashCan || typeof trashCan._latitude !== 'number' || typeof trashCan._longitude !== 'number') {
    return [0, 0, 0];
  }

  const worldPos = latLonToWorld(trashCan._latitude, trashCan._longitude);
  return [worldPos.x * TILE_SIZE, 1, worldPos.z * TILE_SIZE];
}

function handleTouch(event) {
  if (isInteracting.value) return;

  const touch = event.touches ? event.touches[0] : event;

  const rect = renderer.value.domElement.getBoundingClientRect();
  const mouseX = ((touch.clientX - rect.left) / rect.width) * 2 - 1;
  const mouseY = -((touch.clientY - rect.top) / rect.height) * 2 + 1;

  const raycaster = new THREE.Raycaster();
  raycaster.setFromCamera(new THREE.Vector2(mouseX, mouseY), camera.value);

  const allMeshes = [];
  scene.value.traverse(object => {
    if (object.isMesh) {
      allMeshes.push(object);
    }
  });

  const intersects = raycaster.intersectObjects(allMeshes, true);

  if (intersects.length > 0) {
    const clickedPos = intersects[0].point;

    let closestCan = null;
    let closestDistance = Infinity;

    for (const trashCan of safeTrashCans.value) {
      if (!trashCan) continue;

      const position = getPosition(trashCan);
      const distance = new THREE.Vector3(position[0], position[1], position[2])
          .distanceTo(clickedPos);

      if (distance < closestDistance && distance < 2) {
        closestDistance = distance;
        closestCan = trashCan;
      }
    }

    if (closestCan) {
      triggerInteraction(closestCan);
    }
  }
}

function triggerInteraction(trashCan) {
  if (isInteracting.value) return;
  isInteracting.value = true;

  landmarkSystem.showUI('trash-can', trashCan);

  setTimeout(() => {
    mainColor.value = "#444444";
    isInteracting.value = false;
  }, 300);
}

onMounted(() => {
  setTimeout(() => {
    canvasElement.value = document.querySelector('canvas');
    if (canvasElement.value) {
      canvasElement.value.addEventListener('click', handleTouch);
      canvasElement.value.addEventListener('touchstart', handleTouch);
    }
  }, 500);
});

onBeforeUnmount(() => {
  if (canvasElement.value) {
    canvasElement.value.removeEventListener('click', handleTouch);
    canvasElement.value.removeEventListener('touchstart', handleTouch);
  }
});
</script>

<template>
  <TresGroup
      v-for="(trashCan, index) in safeTrashCans"
      :key="trashCan?.id || index"
      :position="getPosition(trashCan)">
    <TresMesh>
      <Cylinder :args="[1.2, 1.2, 2, 16]">
        <TresMeshBasicMaterial :color="mainColor" transparent :opacity="0.3"/>
      </Cylinder>
    </TresMesh>

    <TresMesh>
      <Cylinder :args="[0.7, 0.6, 1.2, 16]">
        <TresMeshBasicMaterial :color="mainColor"/>
      </Cylinder>
    </TresMesh>

    <TresMesh :position="[0, 0.65, 0]">
      <Cylinder :args="[0.75, 0.75, 0.1, 16]">
        <TresMeshBasicMaterial color="#333333"/>
      </Cylinder>
    </TresMesh>

    <TresMesh :position="[0.7, 0, 0]">
      <TresBoxGeometry :args="[0.1, 0.5, 0.1]"/>
      <TresMeshBasicMaterial color="#555555"/>
    </TresMesh>

    <TresMesh :position="[-0.7, 0, 0]">
      <TresBoxGeometry :args="[0.1, 0.5, 0.1]"/>
      <TresMeshBasicMaterial color="#555555"/>
    </TresMesh>
  </TresGroup>
</template>