<script setup>
import SyncStore from "../../../js/sync/SyncStore.js";
import {ref, onMounted, onBeforeUnmount} from 'vue';
import AvatarSelector from './social/AvatarSelector.vue';
import {AVATAR_IDS} from "@/js/auth/Avatars.js";
import AvatarsApi from "@/js/auth/AvatarsApi.js";
import Store from "@/js/Store.js";

defineProps({
  user: {
    type: Object
  }
});

const showSelector = ref(false);
const selectedAvatar = ref(Store.getUser().avatar || AVATAR_IDS[0]);
const previousAvatar = ref(selectedAvatar.value);
const avatarPosition = ref({ x: 0, y: 0 });
const avatarRef = ref(null);

const toggleSelector = () => {
  if (!showSelector.value) {
    updateAvatarPosition();
    previousAvatar.value = selectedAvatar.value;
  }
  showSelector.value = !showSelector.value;
};

const onAvatarSelected = async (id) => {
  // Optimistically update UI
  selectedAvatar.value = id;

  try {
    const response = await AvatarsApi.changeAvatar(id);

    // If response is null (not 2xx status), revert changes
    if (response === null) {
      selectedAvatar.value = previousAvatar.value;
      console.error("Failed to change avatar: API request failed");
    }
  } catch (error) {
    // On error, revert to previous avatar
    selectedAvatar.value = previousAvatar.value;
    console.error("Failed to change avatar:", error);
  }
};

const updateAvatarPosition = () => {
  if (avatarRef.value) {
    const rect = avatarRef.value.getBoundingClientRect();
    avatarPosition.value = {
      x: rect.left + rect.width / 2,
      y: rect.top + rect.height / 2
    };
  }
};

onMounted(() => {
  updateAvatarPosition();
  window.addEventListener('resize', updateAvatarPosition);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateAvatarPosition);
});
</script>

<template>
  <div class="avatar-component">
    <div ref="avatarRef" class="container" @click="toggleSelector">
      <div v-if="!user" class="level-overlay">
        <img src="/images/ui/avatar/level_border.png" alt="">
        <div class="level-number">{{ SyncStore.getLevel()._value.level._value }}</div>
      </div>
      <img :src="`/images/ui/avatar/${selectedAvatar}.png`">
    </div>

    <AvatarSelector
        v-if="showSelector"
        :selected="selectedAvatar"
        :mainPosition="avatarPosition"
        @select="onAvatarSelected"
        @close="showSelector = false"
    />
  </div>
</template>

<style scoped>
.container {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.container img {
  width: 150px;
  image-rendering: pixelated;
}

.level-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.level-number {
  position: absolute;
  bottom: 20px;
  right: 8px;
  width: 40px;
  text-align: center;
  font-size: 1.4rem;
  font-weight: bold;
}
</style>