<template>
  <a href="javascript:void(0)"
     class="menu-button"
     :class="buttonState"
     @click="handleClick">
    <img :src="buttonImage" alt="Menu Button" class="menu-button-bg" />
  </a>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  state: {
    type: String,
    default: 'closed',
    validator: (value) => ['closed', 'open', 'back'].includes(value)
  }
})

const emit = defineEmits(['click'])

function handleClick() {
  emit('click')
}

const buttonState = computed(() => props.state)

const buttonImage = computed(() => {
  const imageMap = {
    'open': '/images/ui/close.png',
    'closed': '/images/ui/open.png',
    'back': '/images/ui/back.png'
  }
  return imageMap[props.state]
})
</script>

<style scoped>
.menu-button {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 300;

  text-decoration: none;
  transition: transform 0.2s;
}

.menu-button img {
  width: 67px;
  height: 67px;
  image-rendering: pixelated;
}
</style>