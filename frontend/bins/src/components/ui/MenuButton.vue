<template>
  <a href="javascript:void(0)" class="section-button" :style="{ transitionDelay }" @click="handleClick">
    {{ label }}
  </a>
</template>

<script setup>
const props = defineProps({
  label: {
    type: String,
    required: true
  },
  transitionDelay: {
    type: String,
    default: '0s'
  }
});

const emit = defineEmits(['click']);

function handleClick() {
  emit('click');
}
</script>

<style scoped>
.section-button {
  display: block;
  padding: 16px 32px;
  background-color: white;
  color: #333;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  width: 200px;
  text-align: center;
  text-decoration: none;
  transition: transform 0.2s, background-color 0.2s;
}

.section-button:hover {
  background-color: #f0f0f0;
  transform: scale(1.05);
  text-decoration: none;
  color: #333;
}

.section-button:active {
  transform: scale(0.98);
}
</style>