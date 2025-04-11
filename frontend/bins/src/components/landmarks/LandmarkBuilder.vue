<script setup>
import { ref, provide, markRaw, shallowRef } from 'vue';
import TrashCanUi from './TrashCanUi.vue';

// Use a map of UI components, properly wrapped with markRaw
const uiComponents = {
  trashcan: markRaw(TrashCanUi)
  // Add more UI types here as needed
};

// Use shallowRef to avoid making nested objects reactive
const uiState = shallowRef({
  type: null,
  show: false
});

// Simple position object with numeric values
const uiPosition = ref({ x: 0, y: 0 });

// Provide a clean API for landmarks to use
provide('landmarkSystem', {
  showUI: (type, data = {}) => {
    // Use a simple string identifier for landmark types
    uiState.value = {
      type,
      show: true,
      data: data || {}
    };
  },
  hideUI: () => {
    uiState.value = {
      ...uiState.value,
      show: false
    };
  }
});

function handleClose() {
  uiState.value = {
    ...uiState.value,
    show: false
  };
}
</script>

<template>
  <slot></slot>

  <!-- Render the appropriate UI component based on type -->
  <component
      v-if="uiState.type && uiComponents[uiState.type] && uiState.show"
      :is="uiComponents[uiState.type]"
      :show="true"
      :data="uiState.data"
      @close="handleClose" />
</template>