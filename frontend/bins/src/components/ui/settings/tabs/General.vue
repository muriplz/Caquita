<template>
  <div class="setting-item">
    <div class="setting-label-container">
      <span class="setting-label">GPS</span>
      <AdditionalInfo message="Please make sure to turn off battery saving options if you are using Wi-fi on phones" />
    </div>
    <ToggleSwitch :model-value="gpsEnabled" @update:model-value="toggleGPS" />
  </div>
  <div class="setting-item">
    <span class="setting-label">Language</span>
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
import AdditionalInfo from "@/components/ui/settings/AdditionalInfo.vue";

const gpsEnabled = computed(() => settingsManager.settings.general.gpsEnabled);

const language = computed({
  get: () => settingsManager.settings.general.language,
  set: (value) => { settingsManager.settings.general.language = value; }
});

function toggleGPS(value) {
  if (value) {
    requestGPS().then(success => {
      settingsManager.settings.general.gpsEnabled = success;
    });
  } else {
    settingsManager.settings.general.gpsEnabled = value;
  }
}

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

.setting-label-container {
  display: flex;
  align-items: center;
}

.setting-label {
  font-weight: 500;
  font-size: 16px;
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