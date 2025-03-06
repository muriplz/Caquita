<template>
  <div class="setting-item">
    <span class="setting-label">Render Distance:</span>
    <div class="slider-container">
      <input
          type="range"
          min="2"
          max="6"
          v-model.number="renderDistance"
          class="slider"
      />
      <span class="slider-value">{{ renderDistance }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import settingsManager from '@/components/ui/settings/settings.js';

// Graphics settings
const renderDistance = computed({
  get: () => settingsManager.settings.graphics.renderDistance,
  set: (value) => { settingsManager.settings.graphics.renderDistance = value; }
});
</script>

<style scoped>
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #333;
}

.setting-label {
  font-weight: 500;
  font-size: 16px;
  user-select: none;
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 200px;
}

.slider {
  width: 100%;
  height: 5px;
  background: #444;
  border-radius: 5px;
  outline: none;
  -webkit-appearance: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 15px;
  height: 15px;
  background: white;
  border-radius: 50%;
  cursor: pointer;
}

.slider::-moz-range-thumb {
  width: 15px;
  height: 15px;
  background: white;
  border-radius: 50%;
  cursor: pointer;
  border: none;
}

.slider-value {
  min-width: 20px;
  text-align: center;
}
</style>