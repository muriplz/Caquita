<template>
  <Transition
      name="dropdown"
      appear
  >
    <div
        v-if="isVisible"
        class="profile-dropdown"
        ref="dropdown"
    >
      <LevelBar />
      <a href="javascript:void(0)" class="dropdown-item" @click="emitEvent('showProfile')">View Profile</a>
      <a href="javascript:void(0)" class="dropdown-item logout" @click="emitEvent('logout')">Logout</a>
    </div>
  </Transition>
</template>

<script setup>
import {nextTick, ref} from 'vue'
import LevelBar from "@/components/ui/auth/LevelBar.vue";

defineProps({
  isVisible: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['showProfile', 'logout', 'close'])
const dropdown = ref(null)

const emitEvent = (eventName) => {
  emit('close')
  nextTick(() => {
    emit(eventName)
  })
}
</script>

<style scoped>
.profile-dropdown {
  position: absolute;
  top: 56px;
  right: 0;
  background-color: white;
  border: 4px solid #333;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  width: 280px;
  z-index: 301;
  transform-origin: top right;
  overflow: hidden;
}

.dropdown-item {
  display: block;
  padding: 10px 15px;
  color: #333;
  text-decoration: none;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background-color: #f0f0f0;
}

.logout:hover {
  background-color: rgba(244, 67, 54, 0.8);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: transform 0.1s ease, opacity 0.1s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: scale(0);
}
</style>