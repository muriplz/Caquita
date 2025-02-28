<template>
  <a href="javascript:void(0)" class="menu-button" @click="handleClick">
    {{ label }}
  </a>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['click'])

function handleClick() {
  emit('click')
}
</script>

<style scoped>
.menu-button {
  display: block;
  padding: 16px 32px;
  background-color: white;
  color: #333;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
  width: 200px;
  text-align: center;
  user-select: none;
  text-decoration: none;
}

.menu-button:hover {
  background-color: #f0f0f0;
  transform: scale(1.05);
  text-decoration: none;
  color: #333;
}

.menu-button:active {
  transform: scale(0.98);
}
</style>