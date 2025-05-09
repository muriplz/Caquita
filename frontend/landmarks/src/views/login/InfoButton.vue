<script setup>
import {ref, onMounted, onUnmounted} from 'vue';

const showTooltip = ref(false);
const componentRef = ref(null);

function toggleTooltip() {
  showTooltip.value = !showTooltip.value;
}

function handleClickOutside(event) {
  if (componentRef.value && !componentRef.value.contains(event.target)) {
    showTooltip.value = false;
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside, true);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside, true);
});
</script>

<template>
  <div class="relative" ref="componentRef">
    <img src="/images/info_button.png" @click.stop="toggleTooltip"/>
    <Transition name="tooltip">
      <div
          v-if="showTooltip"
          class="absolute bottom-full left-0 p-2 w-48 modal"
      >
        Enter your desired username if you don't have an account!
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.tooltip-enter-active,
.tooltip-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.tooltip-enter-from,
.tooltip-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>