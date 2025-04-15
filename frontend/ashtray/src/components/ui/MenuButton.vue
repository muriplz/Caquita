<template>
  <a
      href="javascript:void(0)"
      class="modal px-6 py-3"
      :style="{ transitionDelay }"
      @click="handleClick">
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
</style>