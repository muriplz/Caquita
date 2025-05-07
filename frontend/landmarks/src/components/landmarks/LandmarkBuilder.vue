<script setup>
import { provide, onMounted, onBeforeUnmount } from 'vue';
import TrashCanUI from './TrashCanUI.vue';
import { createApp, h } from 'vue';

// Create a map for our UI instances
const uiInstances = {};

// Create a container for UIs
let uiContainer = null;

onMounted(() => {
  // Create a container for UIs
  uiContainer = document.createElement('div');
  uiContainer.id = 'landmark-ui-container';
  uiContainer.style.position = 'absolute';
  uiContainer.style.top = '0';
  uiContainer.style.left = '0';
  uiContainer.style.width = '100%';
  uiContainer.style.height = '100%';
  uiContainer.style.pointerEvents = 'none';
  uiContainer.style.zIndex = '9999';
  document.body.appendChild(uiContainer);
});

onBeforeUnmount(() => {
  // Clean up UIs
  if (uiContainer) {
    document.body.removeChild(uiContainer);
  }

  // Destroy any active UI instances
  Object.values(uiInstances).forEach(instance => {
    if (instance.app) {
      instance.app.unmount();
    }
    if (instance.element) {
      instance.element.remove();
    }
  });
});

function showUI(type, data) {
  if (type === 'trash-can') {
    const el = document.createElement('div');
    uiContainer.appendChild(el);

    const app = createApp({
      render() {
        return h(TrashCanUI, {
          data,
          onClose: () => {
            app.unmount();
            el.remove();
            delete uiInstances[type];
          }
        });
      }
    });

    app.mount(el);
    uiInstances[type] = { app, element: el };
  }
}

function hideUI(type) {
  if (uiInstances[type]) {
    uiInstances[type].app.unmount();
    uiInstances[type].element.remove();
    delete uiInstances[type];
  }
}

// Provide the landmark system API
provide('landmarkSystem', {
  showUI: (type, data) => {
    if (uiInstances[type]) {
      hideUI(type);
    }
    showUI(type, data);
  },
  hideUI
});
</script>

<template>
  <slot></slot>
</template>