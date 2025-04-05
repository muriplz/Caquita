<script setup>
import {motion} from 'motion-v';
import {ref, onMounted, onBeforeUnmount, computed} from 'vue';
import {AVATAR_IDS} from '@/js/auth/Avatars.js';
import AvatarsApi from "@/js/auth/AvatarsApi.js";
import Store from "@/js/Store.js";

const props = defineProps({
  selected: {
    type: String,
    default: AVATAR_IDS[0]
  },
  mainPosition: {
    type: Object,
    default: () => ({x: 0, y: 0})
  }
});

const emit = defineEmits(['select', 'close']);

const selectorRef = ref(null);
const isVisible = ref(true);
const animatingAvatar = ref(null);
const animationPosition = ref({x: 0, y: 0});
const isAnimating = ref(false);
const unlockedAvatars = ref([]);

const avatarStatus = computed(() => {
  const result = {};

  // Mark all avatars as locked by default
  AVATAR_IDS.forEach(id => {
    result[id] = {unlocked: false};
  });

  // Mark the ones that are unlocked
  unlockedAvatars.value.forEach(avatar => {
    if (typeof avatar === 'string') {
      result[avatar] = {unlocked: true};
    } else if (avatar && avatar.avatar) {
      result[avatar.avatar] = {unlocked: true};
    }
  });

  return result;
});

async function loadUnlockedAvatars() {
  try {
    const avatars = await AvatarsApi.getUnlocked();
    if (avatars && Array.isArray(avatars)) {
      unlockedAvatars.value = avatars;
    } else {
      unlockedAvatars.value = ['trash_can']; // Default avatar is always available
    }
  } catch (error) {
    console.error("Failed to fetch unlocked avatars:", error);
    unlockedAvatars.value = ['trash_can']; // Default avatar is always available
  }
}

function handleClickOutside(event) {
  const element = selectorRef.value?.$el || selectorRef.value;
  if (element && !element.contains(event.target)) {
    closeSelector();
  }
}

function selectAvatar(id, event) {
  // Don't allow selecting locked avatars
  if (isAnimating.value || !avatarStatus.value[id]?.unlocked) return;

  isAnimating.value = true;

  const rect = event.currentTarget.getBoundingClientRect();
  animationPosition.value = {
    x: rect.left + rect.width / 2,
    y: rect.top + rect.height / 2
  };

  animatingAvatar.value = id;

  Store.updateAvatar(id);

  setTimeout(() => {
    emit('select', id);
    setTimeout(() => {
      animatingAvatar.value = null;
      isAnimating.value = false;
    }, 100);
  }, 600);
}

function closeSelector() {
  isVisible.value = false;

  setTimeout(() => {
    emit('close');
  }, 300);
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside);
  loadUnlockedAvatars();
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside);
});
</script>

<template>
  <div ref="selectorRef" class="selector-wrapper">
    <motion.div
        :initial="{ opacity: 0, y: 20 }"
        :animate="{ opacity: isVisible ? 1 : 0, y: isVisible ? 0 : 20 }"
        :transition="{ duration: 0.3 }"
        class="selector"
    >
      <motion.div class="scroller">
        <motion.div
            v-for="avatarId in AVATAR_IDS"
            :key="avatarId"
            class="option"
            :class="{
              selected: selected === avatarId,
              locked: !avatarStatus[avatarId]?.unlocked
            }"
            @click="selectAvatar(avatarId, $event)"
            :whileHover="avatarStatus[avatarId]?.unlocked ? { scale: 1.1 } : {}"
            :whileTap="avatarStatus[avatarId]?.unlocked ? { scale: 0.95 } : {}"
        >
          <img :src="`/images/ui/avatar/${avatarId}.png`" alt="Avatar option">
          <div v-if="!avatarStatus[avatarId]?.unlocked" class="lock-overlay">
            <span class="lock-icon">🔒</span>
          </div>
        </motion.div>
      </motion.div>
    </motion.div>

    <motion.div
        v-if="animatingAvatar"
        class="flying-avatar"
        :initial="{
        opacity: 1,
        scale: 0.5,
        x: animationPosition.x,
        y: animationPosition.y
      }"
        :animate="{
        opacity: 0,
        scale: 1,
        x: mainPosition.x,
        y: mainPosition.y
      }"
        :transition="{ duration: 0.6, ease: 'easeOut' }"
    >
      <img :src="`/images/ui/avatar/${animatingAvatar}.png`" alt="Selected avatar">
    </motion.div>
  </div>
</template>

<style scoped>
.selector-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  pointer-events: none;
}

.selector {
  position: fixed;
  bottom: 25%;
  left: 0;
  width: 100vw;
  background: white;
  border-top: 3px solid #333;
  border-bottom: 3px solid #333;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  padding: 15px 0;
  pointer-events: auto;
  transform-origin: bottom center;
}

.scroller {
  display: flex;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding: 0 15px;
}

.option {
  flex: 0 0 auto;
  width: 70px;
  height: 70px;
  margin: 0 10px;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
}

.option.selected {
  background: rgba(0, 0, 0, 0.1);
}

.option.locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.option img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}

.lock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
}

.lock-icon {
  font-size: 20px;
  color: white;
  filter: drop-shadow(0 0 2px rgba(0, 0, 0, 0.8));
}

.flying-avatar {
  position: fixed;
  width: 70px;
  height: 70px;
  pointer-events: none;
  z-index: 1001;
}

.flying-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
</style>