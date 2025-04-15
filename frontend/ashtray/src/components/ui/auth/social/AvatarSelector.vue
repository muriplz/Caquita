<script setup>
import { motion } from 'motion-v';
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { AVATAR_IDS } from '@/js/auth/Avatars.js';
import AvatarsApi from "@/js/auth/AvatarsApi.js";
import Store from "@/js/Store.js";

const props = defineProps({
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

const currentUserAvatar = computed(() => Store.getUser().avatar);

const avatarStatus = computed(() => {
  const result = {};
  AVATAR_IDS.forEach(id => {
    result[id] = {unlocked: false};
  });

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
      unlockedAvatars.value = ['trash_can'];
    }
  } catch (error) {
    console.error("Failed to fetch unlocked avatars:", error);
    unlockedAvatars.value = ['trash_can'];
  }
}

function handleClickOutside(event) {
  const element = selectorRef.value?.$el || selectorRef.value;
  if (element && !element.contains(event.target)) {
    closeSelector();
  }
}

function selectAvatar(id, event) {
  if (isAnimating.value || !avatarStatus.value[id]?.unlocked || id === currentUserAvatar.value) return;

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
  <div ref="selectorRef" class="fixed inset-0 z-50 pointer-events-none">
    <motion.div
        :initial="{ opacity: 0, y: 20 }"
        :animate="{ opacity: isVisible ? 1 : 0, y: isVisible ? 0 : 20 }"
        :transition="{ duration: 0.3 }"
        class="fixed bottom-1/4 w-full bg-white border-t-2 border-b-2 border-gray-800 shadow-md py-4 pointer-events-auto"
    >
      <div class="flex overflow-x-auto overflow-y-hidden px-4">
        <div
            v-for="avatarId in AVATAR_IDS"
            :key="avatarId"
            class="flex-none w-16 h-16 mx-2 rounded relative cursor-pointer"
            :class="{
            'bg-gray-200': avatarId === currentUserAvatar.value,
            'opacity-60 cursor-not-allowed': !avatarStatus[avatarId]?.unlocked || avatarId === currentUserAvatar.value
          }"
            @click="selectAvatar(avatarId, $event)"
        >
          <img :src="`/images/ui/avatar/${avatarId}.png`" alt="Avatar option" class="w-full h-full object-contain" style="image-rendering: pixelated;">
          <div v-if="!avatarStatus[avatarId]?.unlocked" class="absolute inset-0 flex items-center justify-center bg-black bg-opacity-30 rounded">
            <img src="/images/ui/lock.png" alt="Locked" class="w-6 h-6">
          </div>
        </div>
      </div>
    </motion.div>

    <motion.div
        v-if="animatingAvatar"
        class="fixed w-16 h-16 pointer-events-none z-50"
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
      <img :src="`/images/ui/avatar/${animatingAvatar}.png`" alt="Selected avatar" class="w-full h-full object-contain">
    </motion.div>
  </div>
</template>