<template>
  <div class="setting-item">
    <span class="setting-label">Location (GPS):</span>
    <ToggleSwitch :model-value="gpsEnabled" @update:model-value="toggleGPS" />
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
</template>

<script setup>
import { computed } from 'vue';
import ToggleSwitch from '../ToggleSwitch.vue';
import settingsManager from '@/components/ui/settings/settings.js';

// General settings
const gpsEnabled = computed(() => settingsManager.settings.general.gpsEnabled);

const language = computed({
  get: () => settingsManager.settings.general.language,
  set: (value) => { settingsManager.settings.general.language = value; }
});

// Toggle GPS function
function toggleGPS(value) {
  if (value) {
    // If toggling on, first request permissions
    requestGPS().then(success => {
      // Only update the setting if permission was granted
      settingsManager.settings.general.gpsEnabled = success;
    });
  } else {
    // If toggling off, just update the setting
    settingsManager.settings.general.gpsEnabled = value;
  }
}

// Request GPS permissions
function requestGPS() {
  if (!navigator.geolocation) {
    return Promise.resolve(false);
  }

  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
        () => {
          resolve(true);
        },
        () => {
          resolve(false);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
    );
  });
}
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

.select-dropdown {
  background-color: #333;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 12px;
  width: 200px;
}
</style>