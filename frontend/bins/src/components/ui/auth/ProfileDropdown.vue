<template>
  <Transition
      name="dropdown"
      enter-active-class="animate-enter"
      leave-active-class="animate-leave"
      :duration="{ enter: 300, leave: 200 }"
      appear
  >
    <div
        v-if="isVisible"
        class="profile-dropdown"
        ref="dropdown"
    >
      <div v-if="!Store.getUser()">
        <a href="javascript:void(0)" class="dropdown-item" @click="emitEvent('showLogin')">Login</a>
        <a href="javascript:void(0)" class="dropdown-item" @click="emitEvent('showRegister')">Register</a>
      </div>
      <div v-else>
        <div class="profile-info">
          <span class="username">@{{ Store.getUser().username }}</span>
        </div>
        <a href="javascript:void(0)" class="dropdown-item" @click="emitEvent('showProfile')">View Profile</a>
        <a href="javascript:void(0)" class="dropdown-item logout" @click="emitEvent('logout')">Logout</a>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { defineEmits, defineProps, onMounted, onBeforeUnmount, ref, nextTick } from 'vue'
import Store from "@/js/auth/store.js"

const props = defineProps({
  isVisible: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['showLogin', 'showRegister', 'showProfile', 'logout', 'close'])
const dropdown = ref(null)

const emitEvent = (eventName) => {
  // Close dropdown first, then emit the event
  emit('close')

  // Use nextTick to make sure the dropdown is closed before triggering the next action
  nextTick(() => {
    emit(eventName)
  })
}

const handleClickOutside = (event) => {
  if (props.isVisible && dropdown.value && !dropdown.value.contains(event.target)) {
    // Check if the click was outside the dropdown and not on the profile button
    if (!event.target.closest('.profile-button')) {
      emit('close')
    }
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.profile-dropdown {
  position: absolute;
  top: 40px;
  right: 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  width: 180px;
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

.profile-info {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.username {
  display: block;
  font-weight: 600;
  color: #333;
}

.logout:hover {
  background-color: rgba(244, 67, 54, 0.8); /* 0.8 is the alpha value for 80% opacity */
}

/* Animations */
.animate-enter {
  animation: dropdown-in 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.animate-leave {
  animation: dropdown-out 0.2s cubic-bezier(0.4, 0, 1, 1) forwards;
}

@keyframes dropdown-in {
  0% {
    opacity: 0;
    transform: scale(0.9) translateY(-10px);
  }
  70% {
    opacity: 1;
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes dropdown-out {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(0.95) translateY(-5px);
  }
}
</style>