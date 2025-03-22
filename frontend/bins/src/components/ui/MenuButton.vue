<template>
  <a href="javascript:void(0)"
     class="menu-button"
     :class="[buttonState, { 'flip-right': isFlippingRight, 'flip-left': isFlippingLeft }]"
     @click="handleClick">
    <div class="coin">
      <!-- Front face of coin -->
      <div class="coin-face coin-front">
        <img :src="currentImage" alt="Menu Button Front" />
      </div>

      <!-- Back face of coin -->
      <div class="coin-face coin-back">
        <img :src="nextImage" alt="Menu Button Back" />
      </div>

      <!-- Cylindrical edge -->
      <div class="coin-edge"></div>
    </div>
  </a>
</template>

<script setup>
import { computed, ref } from 'vue';

const props = defineProps({
  state: {
    type: String,
    default: 'closed',
    validator: (value) => ['closed', 'open', 'back'].includes(value)
  }
})

const emit = defineEmits(['click'])
const isFlippingRight = ref(false)
const isFlippingLeft = ref(false)
const showingFront = ref(true)
const transitionState = ref(props.state)

// Animation duration in ms
const ANIMATION_DURATION = 800;

// Set the next state that will appear after flipping
function getNextState(currentState, direction) {
  if (direction === 'right') {
    if (currentState === 'closed') return 'open'
    if (currentState === 'open') return 'back'
    return 'closed'
  } else {
    if (currentState === 'back') return 'open'
    if (currentState === 'open') return 'closed'
    return 'back'
  }
}

function handleClick() {
  emit('click')
}

function flipRight() {
  if (isFlippingRight.value || isFlippingLeft.value) return

  const nextState = getNextState(transitionState.value, 'right')
  nextImageState.value = nextState
  isFlippingRight.value = true

  setTimeout(() => {
    transitionState.value = nextState
    showingFront.value = !showingFront.value
    isFlippingRight.value = false
  }, ANIMATION_DURATION / 2)
}

function flipLeft() {
  if (isFlippingRight.value || isFlippingLeft.value) return

  const nextState = getNextState(transitionState.value, 'left')
  nextImageState.value = nextState
  isFlippingLeft.value = true

  setTimeout(() => {
    transitionState.value = nextState
    showingFront.value = !showingFront.value
    isFlippingLeft.value = false
  }, ANIMATION_DURATION / 2)
}

const buttonState = computed(() => props.state)
const nextImageState = ref(props.state)

const imageMap = {
  'open': '/images/ui/close.png',
  'closed': '/images/ui/open.png',
  'back': '/images/ui/back.png'
}

const currentImage = computed(() => {
  return imageMap[transitionState.value]
})

const nextImage = computed(() => {
  return imageMap[nextImageState.value]
})

// Expose these functions to the parent component
defineExpose({
  flipRight,
  flipLeft
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
  perspective: 1000px;
}

.coin {
  position: relative;
  transform-style: preserve-3d;
  width: 67px;
  height: 67px;
  transform: translateZ(-6px);
}

.coin-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 50%;
  overflow: hidden;
}

.coin-front {
  transform: translateZ(6px);
}

.coin-back {
  transform: rotateY(180deg) translateZ(6px);
}

.coin-face img {
  width: 100%;
  height: 100%;
  image-rendering: pixelated;
}

/* Create the cylindrical edge */
.coin-edge {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  border-radius: 50%;
}

/* Generate the edge segments to create a cylinder */
.coin-edge::before {
  content: '';
  position: absolute;
  width: 12px;
  height: 100%;
  background: #555555;
  left: 50%;
  top: 0;
  transform: translateX(-50%) rotateY(0deg) translateZ(-6px);
  transform-origin: center center;
  box-shadow:
      0 0 0 67px #555555,
      calc(67px * 0.383) calc(67px * 0.924) 0 -6px #555555,
      calc(67px * 0.707) calc(67px * 0.707) 0 -6px #555555,
      calc(67px * 0.924) calc(67px * 0.383) 0 -6px #555555,
      calc(67px * 0.383) calc(-67px * 0.924) 0 -6px #555555,
      calc(67px * 0.707) calc(-67px * 0.707) 0 -6px #555555,
      calc(67px * 0.924) calc(-67px * 0.383) 0 -6px #555555,
      calc(-67px * 0.383) calc(67px * 0.924) 0 -6px #555555,
      calc(-67px * 0.707) calc(67px * 0.707) 0 -6px #555555,
      calc(-67px * 0.924) calc(67px * 0.383) 0 -6px #555555,
      calc(-67px * 0.383) calc(-67px * 0.924) 0 -6px #555555,
      calc(-67px * 0.707) calc(-67px * 0.707) 0 -6px #555555,
      calc(-67px * 0.924) calc(-67px * 0.383) 0 -6px #555555;
}

.flip-right .coin {
  animation: flipCoinRight 0.8s cubic-bezier(0.3, 0.7, 0.4, 1.4) forwards;
}

.flip-left .coin {
  animation: flipCoinLeft 0.8s cubic-bezier(0.7, 0.3, 0.4, 1.4) forwards;
}

@keyframes flipCoinRight {
  0% {
    transform: translateZ(-6px) rotateY(0) translateX(0) translateY(0);
  }
  10% {
    transform: translateZ(-6px) rotateY(20deg) translateX(3px) translateY(-2px) scale(1.05);
  }
  25% {
    transform: translateZ(-6px) rotateY(45deg) translateX(7px) translateY(0) scale(1.1);
  }
  40% {
    transform: translateZ(-6px) rotateY(80deg) translateX(5px) translateY(2px) scale(1.08);
  }
  60% {
    transform: translateZ(-6px) rotateY(100deg) translateX(3px) translateY(0) scale(1.05);
  }
  75% {
    transform: translateZ(-6px) rotateY(135deg) translateX(4px) translateY(-1px) scale(1.02);
  }
  90% {
    transform: translateZ(-6px) rotateY(170deg) translateX(2px) translateY(0) scale(1);
  }
  100% {
    transform: translateZ(-6px) rotateY(180deg) translateX(0) translateY(0);
  }
}

@keyframes flipCoinLeft {
  0% {
    transform: translateZ(-6px) rotateY(0) translateX(0) translateY(0);
  }
  10% {
    transform: translateZ(-6px) rotateY(-20deg) translateX(-3px) translateY(-2px) scale(1.05);
  }
  25% {
    transform: translateZ(-6px) rotateY(-45deg) translateX(-7px) translateY(0) scale(1.1);
  }
  40% {
    transform: translateZ(-6px) rotateY(-80deg) translateX(-5px) translateY(2px) scale(1.08);
  }
  60% {
    transform: translateZ(-6px) rotateY(-100deg) translateX(-3px) translateY(0) scale(1.05);
  }
  75% {
    transform: translateZ(-6px) rotateY(-135deg) translateX(-4px) translateY(-1px) scale(1.02);
  }
  90% {
    transform: translateZ(-6px) rotateY(-170deg) translateX(-2px) translateY(0) scale(1);
  }
  100% {
    transform: translateZ(-6px) rotateY(-180deg) translateX(0) translateY(0);
  }
}
</style>