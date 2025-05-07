<template>
  <a
      href="javascript:void(0)"
      class="modal px-6 py-3 font-bold"
      :style="{ transitionDelay }"
      @click="handleClick">
    {{ label }}
  </a>
</template>

<script setup>

const emit = defineEmits(['click']);

defineProps({
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