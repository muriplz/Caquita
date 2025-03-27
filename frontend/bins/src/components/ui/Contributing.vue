<template>
  <div class="contributing-modal">
    <div class="contributing-header">
      <h2>Contribute!</h2>
    </div>
    <p>
      The contributions are submitted through our Petition Portal, called
      <a href="https://landmarks.caquita.app"  target="_blank" rel="noopener noreferrer">Landmarks</a>.
    </p>
  </div>
</template>

<script setup>
import {getIpAddress} from "@/js/Static.js";

const emit = defineEmits(['openMaterial', 'close']);

function openMaterial(type) {
  emit('openMaterial', type);
}
</script>

<style scoped>
.contributing-modal {
  background-color: #ffffff;
  padding: 16px;
  margin: 0 8px;
  border: 4px solid #373737;
}

.contributing-header {
  text-align: center;
  margin-bottom: 12px;
}

.contributing-header h2 {
  margin: 0;
  font-size: 24px;
  color: #373737;
}

p {
  color: #373737;
}

.material-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 20px;
}

.landmark-icon {
  height: 100px;
  image-rendering: pixelated;
}

.material-icon:hover {
  transform: scale(1.1);
  background-color: #e0e0e0;
}
</style>