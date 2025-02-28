<template>
  <div class="settings-container">
    <h2>Settings</h2>
    <div class="settings-content">
      <div class="setting-item">
        <span class="setting-label">Location (GPS):</span>
        <div class="toggle-switch">
          <div class="toggle-track" @click="toggleGPS" :class="{ 'active': isActive }">
            <div class="toggle-indicator" :class="{ 'active': isActive }"></div>
            <span class="toggle-label off" :class="{ 'hidden': isActive }">OFF</span>
            <span class="toggle-label on" :class="{ 'hidden': !isActive }">ON</span>
          </div>
        </div>
      </div>
    </div>

    <div class="action-buttons">
      <button @click="$emit('close')" class="close-button">Close</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useGeolocation } from '@vueuse/core';
import { disableGPSControl, enableGPSControl } from "@/components/player/GPSPlayerIntegration.js";
import { startGPSTracking, stopGPSTracking } from "@/components/player/GPSTracker.js";

// Use localStorage to persist the GPS setting
const STORAGE_KEY = 'gps-enabled';
const isActive = ref(false);

// Initialize the geolocation service with immediate=false
// so we can control when it starts based on saved preferences
const {
  coords,
  resume,
  pause,
  isSupported
} = useGeolocation({
  enableHighAccuracy: true,
  maximumAge: 0,
  timeout: 30000,
  immediate: false
});

// Check if GPS data is available
const hasGPSData = computed(() =>
    coords.value.latitude !== undefined &&
    coords.value.longitude !== undefined
);

// Load saved preference and initialize GPS on component mount
onMounted(() => {
  loadSavedPreference();
});

// Save settings when component is destroyed
onBeforeUnmount(() => {
  savePreference();
});

// Load saved GPS preference from localStorage
function loadSavedPreference() {
  try {
    const savedSetting = localStorage.getItem(STORAGE_KEY);

    if (savedSetting === 'true') {
      // If GPS was previously enabled, restart it
      isActive.value = true;
      activateGPS();
    }
  } catch (e) {
    console.error('Error loading GPS preference:', e);
  }
}

// Save current GPS preference to localStorage
function savePreference() {
  try {
    localStorage.setItem(STORAGE_KEY, isActive.value.toString());
  } catch (e) {
    console.error('Error saving GPS preference:', e);
  }
}

// Toggle GPS state
function toggleGPS() {
  isActive.value = !isActive.value;

  if (isActive.value) {
    activateGPS();
  } else {
    deactivateGPS();
  }

  // Save the updated preference
  savePreference();
}

// Activate GPS tracking and player control
function activateGPS() {
  if (isSupported.value) {
    resume();
    startGPSTracking();
    enableGPSControl();

    // Request permission explicitly if needed
    if (navigator.permissions && navigator.permissions.query) {
      navigator.permissions.query({ name: 'geolocation' })
          .then(result => {
            if (result.state === 'denied') {
              // If permission is denied, update UI accordingly
              isActive.value = false;
              savePreference();
              console.error('Geolocation permission denied');
            }
          })
          .catch(err => console.error('Permission query error:', err));
    }
  } else {
    console.error('Geolocation not supported');
    isActive.value = false;
    savePreference();
  }
}

// Deactivate GPS tracking and player control
function deactivateGPS() {
  pause();
  stopGPSTracking();
  disableGPSControl();

  // Clear the permission if possible
  if (navigator.permissions && navigator.permissions.revoke) {
    try {
      // Note: revoke() is experimental and may not work in all browsers
      navigator.permissions.revoke({ name: 'geolocation' })
          .catch(err => console.warn('Permission revoke not supported:', err));
    } catch (e) {
      console.warn('Permission revoke failed:', e);
    }
  }
}

defineEmits(['close']);
</script>

<style scoped>
.settings-container {
  width: 90%;
  max-width: 600px;
  background-color: black;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.setting-label {
  font-weight: 500;
  font-size: 16px;
  user-select: none;
}

.toggle-switch {
  position: relative;
  display: inline-block;
}

.toggle-track {
  width: 70px;
  height: 30px;
  border-radius: 15px;
  background-color: #f44336;
  transition: background-color 0.3s;
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 0 5px;
}

.toggle-track.active {
  background-color: #4CAF50;
}

.toggle-indicator {
  position: absolute;
  left: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background-color: white;
  transition: transform 0.3s ease;
}

.toggle-indicator.active {
  transform: translateX(50px);
}

.toggle-label {
  color: white;
  font-weight: bold;
  font-size: 12px;
  transition: opacity 0.2s;
  user-select: none;
}

.toggle-label.off {
  position: absolute;
  right: 8px;
}

.toggle-label.on {
  position: absolute;
  left: 8px;
}

.toggle-label.hidden {
  opacity: 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 16px;
}

.close-button {
  padding: 10px 20px;
  background-color: #f2f2f2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
}

.close-button:hover {
  background-color: #e0e0e0;
}

.location-info p {
  margin: 6px 0;
}
</style>