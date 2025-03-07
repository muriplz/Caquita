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
      <div class="tabs-container">
        <div
            v-for="tab in ['general', 'controls', 'graphics']"
            :key="tab"
            class="tab-content"
            :class="{
              'active': activeTab === tab,
              'slide-left': slideDirection === 'left' && (activeTab === tab || previousTab === tab),
              'slide-right': slideDirection === 'right' && (activeTab === tab || previousTab === tab),
              'tab-left': tab === previousTab && slideDirection === 'left',
              'tab-right': tab === previousTab && slideDirection === 'right',
              'tab-incoming-left': tab === activeTab && slideDirection === 'left',
              'tab-incoming-right': tab === activeTab && slideDirection === 'right'
            }"
        >
          <GeneralTab v-if="tab === 'general'" />
          <ControlsTab v-if="tab === 'controls'" />
          <GraphicsTab v-if="tab === 'graphics'" />
        </div>
      </div>
    </div>

    <div class="action-buttons">
      <button @click="resetSettings" class="reset-button">Reset</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import GeneralTab from './tabs/General.vue';
import ControlsTab from './tabs/Controls.vue';
import GraphicsTab from './tabs/Graphics.vue';
import settingsManager from '@/components/ui/settings/settings.js';

// Active tab state
const activeTab = ref('general');
const previousTab = ref('');
const slideDirection = ref('');

// Watch for tab changes to set the correct transition direction
watch(activeTab, (newTab, oldTab) => {
  if (oldTab) {
    previousTab.value = oldTab;

    // Determine tabs order for directional animation
    const tabOrder = ['general', 'controls', 'graphics'];
    const newIndex = tabOrder.indexOf(newTab);
    const oldIndex = tabOrder.indexOf(oldTab);

    slideDirection.value = newIndex > oldIndex ? 'left' : 'right';
  }
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
  position: relative;
}

.tabs-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.tab-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: none;
  flex-direction: column;
  overflow-y: auto;
  transition: transform 0.2s ease;
}

.tab-content.active {
  display: flex;
}

/* Animation classes */
.tab-content.slide-left,
.tab-content.slide-right {
  display: flex;
}

.tab-content.tab-left {
  transform: translateX(-100%);
}

.tab-content.tab-right {
  transform: translateX(100%);
}

.tab-content.tab-incoming-left {
  transform: translateX(0);
  animation: slide-from-right 0.2s ease;
}

.tab-content.tab-incoming-right {
  transform: translateX(0);
  animation: slide-from-left 0.2s ease;
}

@keyframes slide-from-right {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

@keyframes slide-from-left {
  from { transform: translateX(-100%); }
  to { transform: translateX(0); }
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
}

.close-button:hover {
  background-color: #444;
}
</style>