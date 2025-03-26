<template>
  <div class="contributing-modal">
    <div class="contributing-header">
      <h2>Select Landmark Type</h2>
    </div>
    <div class="material-grid">
      <div @click="openMaterial('cans')">
        <img class="landmark-icon" src="@/assets/landmarks/can.svg" alt="Landmark" />
        <p>Cans</p>
      </div>
      <div @click="openMaterial('plastic')">
        <img class="landmark-icon" src="@/assets/landmarks/plastic.svg" alt="Landmark" />
        <p>Plastic</p>
      </div>
      <div @click="openMaterial('cardboard')">
        <img class="landmark-icon" src="@/assets/landmarks/cardboard.svg" alt="Landmark" />
        <p>Cardboard</p>
      </div>
      <div class="material-icon" @click="openMaterial('glass')">
        <img class="landmark-icon" src="@/assets/landmarks/glass.svg" alt="Landmark" />
        <p>Glass</p>
      </div>
    </div>
  </div>
</template>

<script setup>
const emit = defineEmits(['openMaterial', 'close']);

function openMaterial(type) {
  emit('openMaterial', type);
}
</script>

<style scoped>
.contributing-modal {
  background-color: #373737;
  padding: 16px;
  border-radius: 24px;
}

.contributing-header {
  text-align: center;
  margin-bottom: 12px;
}

.contributing-header h2 {
  margin: 0;
  font-size: 24px;
}

.material-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 20px;
}

.landmark-icon {
  width: 100px;
  height: 100px;

}

.material-icon:hover {
  transform: scale(1.1);
  background-color: #e0e0e0;
}
</style>