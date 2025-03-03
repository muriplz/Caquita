<template>
  <div class="settings-modal">
    <h2>Settings</h2>

    <div class="tabs">
      <button
          @click="activeTab = 'general'"
          :class="{ 'active': activeTab === 'general' }"
          class="tab-button"
      >
        General
      </button>
      <button
          @click="activeTab = 'controls'"
          :class="{ 'active': activeTab === 'controls' }"
          class="tab-button"
      >
        Controls
      </button>
      <button
          @click="activeTab = 'graphics'"
          :class="{ 'active': activeTab === 'graphics' }"
          class="tab-button"
      >
        Graphics
      </button>
    </div>

    <div class="settings-content">
      <!-- General Tab -->
      <div v-if="activeTab === 'general'" class="tab-content">
        <div class="setting-item">
          <span class="setting-label">Location (GPS):</span>
          <ToggleSwitch v-model="gpsEnabled" />
        </div>
        <div class="setting-item">
          <span class="setting-label">Language:</span>
          <select v-model="language" class="select-dropdown">
            <option value="en">English</option>
            <option value="fr">French</option>
            <option value="es">Spanish</option>
            <option value="de">German</option>
          </select>
        </div>
      </div>

      <!-- Controls Tab -->
      <div v-if="activeTab === 'controls'" class="tab-content">
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
      </div>

      <!-- Graphics Tab -->
      <div v-if="activeTab === 'graphics'" class="tab-content">
        <div class="setting-item">
          <span class="setting-label">Render Distance:</span>
          <div class="slider-container">
            <input
                type="range"
                min="1"
                max="10"
                v-model.number="renderDistance"
                class="slider"
            />
            <span class="slider-value">{{ renderDistance }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="action-buttons">
      <button @click="resetSettings" class="reset-button">Reset</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import ToggleSwitch from './ToggleSwitch.vue';
import settingsManager from '@/components/ui/settings/settings.js';

// Active tab state
const activeTab = ref('general');

// General settings
const gpsEnabled = computed({
  get: () => settingsManager.settings.general.gpsEnabled,
  set: (value) => { settingsManager.settings.general.gpsEnabled = value; }
});

const language = computed({
  get: () => settingsManager.settings.general.language,
  set: (value) => { settingsManager.settings.general.language = value; }
});

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

// Graphics settings
const renderDistance = computed({
  get: () => settingsManager.settings.graphics.renderDistance,
  set: (value) => { settingsManager.settings.graphics.renderDistance = value; }
});

// Reset settings
function resetSettings() {
  if (confirm("Are you sure you want to reset all settings to defaults?")) {
    // Store current tab
    const currentTab = activeTab.value;

    // Reset settings through the manager
    settingsManager.resetAllSettings();

    // Force UI refresh by cycling tabs
    activeTab.value = currentTab === 'general' ? 'controls' : 'general';

    // Then return to original tab
    setTimeout(() => {
      activeTab.value = currentTab;
    }, 50);
  }
}

defineEmits(['close']);
</script>

<style scoped>
.settings-modal {
  width: 90%;
  max-width: 600px;
  height: calc(100% - 100px);
  max-height: 600px;
  background-color: black;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  padding: 24px;
  display: flex;
  flex-direction: column;
  color: white;
  position: relative;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #333;
  margin-bottom: 15px;
}

.tab-button {
  padding: 10px 20px;
  background-color: transparent;
  border: none;
  color: #aaa;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}

.tab-button.active {
  color: white;
  border-bottom: 2px solid #4D8061;
}

.tab-button:hover {
  color: white;
}

.settings-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
}

.tab-content {
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

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

.select-dropdown {
  background-color: #333;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 12px;
  width: 200px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 16px;
}

.reset-button {
  padding: 10px 20px;
  background-color: #8B0000;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
}

.reset-button:hover {
  background-color: #A52A2A;
}

.close-button {
  padding: 10px 20px;
  background-color: #333;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
}

.close-button:hover {
  background-color: #444;
}
</style>