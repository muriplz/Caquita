<template>
  <a href="javascript:void(0)" class="section-button" :style="{ transitionDelay }" @click="handleClick">
    {{ label }}
  </a>
</template>

<script setup>

const emit = defineEmits(['click']);
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
  border: 4px solid #333;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  width: 200px;
  text-align: center;
  text-decoration: none;
  transition: transform 0.2s, background-color 0.2s;
}

</style>