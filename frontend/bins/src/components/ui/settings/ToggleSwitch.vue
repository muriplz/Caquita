<template>
  <div class="toggle-switch">
    <div class="toggle-track" @click="toggle" :class="{ 'active': modelValue }">
      <div class="toggle-indicator" :class="{ 'active': modelValue }"></div>
      <span class="toggle-label off" :class="{ 'hidden': modelValue }">OFF</span>
      <span class="toggle-label on" :class="{ 'hidden': !modelValue }">ON</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue']);

function toggle() {
  emit('update:modelValue', !props.modelValue);
}
</script>

<style scoped>
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
</style>