<template>
  <div class="setting-item">
    <span class="setting-label">Invert Y-Axis:</span>
    <ToggleSwitch v-model="invertYAxis" />
  </div>
  <div class="setting-item">
    <span class="setting-label">Camera Sensitivity X:</span>
    <div class="slider-container">
      <input
          type="range"
          min="1"
          max="10"
          v-model.number="cameraSensitivityX"
          class="slider"
      />
      <span class="slider-value">{{ cameraSensitivityX }}</span>
    </div>
  </div>
  <div class="setting-item">
    <span class="setting-label">Camera Sensitivity Y:</span>
    <div class="slider-container">
      <input
          type="range"
          min="1"
          max="10"
          v-model.number="cameraSensitivityY"
          class="slider"
      />
      <span class="slider-value">{{ cameraSensitivityY }}</span>
    </div>
  </div>
  <div class="setting-item">
    <span class="setting-label">Zoom Speed:</span>
    <div class="slider-container">
      <input
          type="range"
          min="0.1"
          max="3"
          step="0.1"
          v-model.number="zoomSpeed"
          class="slider"
      />
      <span class="slider-value">{{ zoomSpeed }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import ToggleSwitch from '../ToggleSwitch.vue';
import settingsManager from '@/components/ui/settings/settings.js';

// Control settings
const invertYAxis = computed({
  get: () => settingsManager.settings.controls.invertYAxis,
  set: (value) => { settingsManager.settings.controls.invertYAxis = value; }
});

const cameraSensitivityX = computed({
  get: () => settingsManager.settings.controls.cameraSensitivityX,
  set: (value) => { settingsManager.settings.controls.cameraSensitivityX = value; }
});

const cameraSensitivityY = computed({
  get: () => settingsManager.settings.controls.cameraSensitivityY,
  set: (value) => { settingsManager.settings.controls.cameraSensitivityY = value; }
});

const zoomSpeed = computed({
  get: () => settingsManager.settings.controls.zoomSpeed,
  set: (value) => { settingsManager.settings.controls.zoomSpeed = value; }
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