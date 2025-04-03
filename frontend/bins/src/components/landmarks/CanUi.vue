<script setup>
import { ref, watch, onMounted } from 'vue';

const props = defineProps({
  show: Boolean,
  clickPosition: Object
});

const emit = defineEmits(['close']);
const isTransitioning = ref(false);
const initialPosition = ref({ x: 0, y: 0 });

function closeUi() {
  emit('close');
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    // When showing, set the initial position to the click position
    initialPosition.value = {
      x: props.clickPosition?.x || window.innerWidth / 2,
      y: props.clickPosition?.y || window.innerHeight / 2
    };
    isTransitioning.value = true;

    // Reset transitioning flag after animation completes
    setTimeout(() => {
      isTransitioning.value = false;
    }, 300); // Same duration as transition
  }
});
</script>

<template>
  <Transition name="can-ui">
    <div v-if="show" class="can-ui" :class="{ 'transitioning': isTransitioning }" :style="isTransitioning ? {
      '--start-x': initialPosition.x + 'px',
      '--start-y': initialPosition.y + 'px'
    } : {}">
      <div class="header">
        <button class="close-btn" @click.stop="closeUi">×</button>
      </div>
      <div class="content">
        Hello world
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.can-ui {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  z-index: 999;
  min-width: 200px;
  min-height: 120px;
}

.header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.content {
  text-align: center;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.close-btn:hover {
  color: #000;
}

.can-ui-enter-active,
.can-ui-leave-active {
  transition: all 0.3s ease;
}

.can-ui.transitioning {
  animation: moveToCenter 0.3s ease forwards;
}

@keyframes moveToCenter {
  0% {
    left: var(--start-x);
    top: var(--start-y);
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.5);
  }
  100% {
    left: 50%;
    top: 50%;
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

.can-ui-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.8);
}
</style>