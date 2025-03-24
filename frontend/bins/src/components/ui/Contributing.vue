<template>
  <div class="contributing-modal">
    <div class="contributing-header">
      <h2>Select Material Type</h2>
    </div>
    <div class="material-grid">
      <div class="material-icon" @click="openMaterial('cans')">C</div>
      <div class="material-icon" @click="openMaterial('plastic')">P</div>
      <div class="material-icon" @click="openMaterial('paper')">P</div>
      <div class="material-icon" @click="openMaterial('glass')">G</div>
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
  background-color: white;
  padding: 30px;
  border-radius: 12px;
  max-width: 400px;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.contributing-header {
  text-align: center;
  margin-bottom: 10px;
}

.contributing-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.material-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 20px;
}

.material-icon {
  width: 80px;
  height: 80px;
  background-color: #f0f0f0;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 32px;
  font-weight: bold;
  color: #333;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
  margin: 0 auto;
}

.material-icon:hover {
  transform: scale(1.1);
  background-color: #e0e0e0;
}
</style>