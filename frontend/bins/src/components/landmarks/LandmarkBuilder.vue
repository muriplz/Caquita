<script setup>
import { ref, provide } from 'vue';

const activeUIComponent = ref(null);
const uiPosition = ref({ x: 0, y: 0 });

provide('landmarkSystem', {
  showUI: (component, position) => {
    uiPosition.value = position;
    activeUIComponent.value = component;
  },
  hideUI: () => {
    activeUIComponent.value = null;
  }
});
</script>

<template>
  <slot></slot>

  <component
      v-if="activeUIComponent"
      :is="activeUIComponent"
      :position="uiPosition"
      :show="!!activeUIComponent"
      @close="activeUIComponent = null" />
</template>