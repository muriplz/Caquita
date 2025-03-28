<template>
  <Transition
      name="dropdown"
      enter-active-class="dropdown-enter"
      leave-active-class="dropdown-leave"
      :duration="{ enter: 300, leave: 150 }"
      appear
  >
    <div
        v-if="isVisible"
        class="profile-dropdown"
        ref="dropdown"
    >
      <div class="profile-info">
        <span class="username">@{{ Store.getUser().username }}</span>
      </div>
      <a href="javascript:void(0)" class="dropdown-item" @click="emitEvent('showProfile')">View Profile</a>
      <a href="javascript:void(0)" class="dropdown-item logout" @click="emitEvent('logout')">Logout</a>
    </div>
  </Transition>
</template>

<script setup>
import {nextTick, onBeforeUnmount, onMounted, ref} from 'vue'
import Store from "@/js/Store.js"
import KofiLogo from "@/assets/icons/KofiLogo.vue";

const props = defineProps({
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

const handleClickOutside = (event) => {
  if (props.isVisible && dropdown.value && !dropdown.value.contains(event.target)) {
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
  top: 56px;
  right: 0;
  background-color: white;
  border: 3px solid black;
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
  border-bottom: 2px solid black;
  margin: 0 8px;
}

.username {
  display: block;
  font-weight: bold;
  font-size: 1.1rem;
  color: black;
}

.logout:hover {
  background-color: rgba(244, 67, 54, 0.8);
}

.dropdown-enter {
  animation: paper-fall 400ms cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dropdown-leave {
  animation: paper-fold 300ms ease-in forwards;
}


@keyframes paper-fall {
  0% {
    opacity: 0;
    transform: translateY(-30px) rotateX(45deg);
    box-shadow: 0 0 0 rgba(0, 0, 0, 0);
  }
  50% {
    opacity: 1;
    transform: translateY(10px) rotateX(-10deg);
  }
  70% {
    transform: translateY(-5px) rotateX(5deg);
  }
  100% {
    transform: translateY(0) rotateX(0);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  }
}

@keyframes paper-fold {
  0% {
    opacity: 1;
    transform: translateY(0) rotateX(0);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  }
  100% {
    opacity: 0;
    transform: translateY(-20px) rotateX(60deg);
    box-shadow: 0 0 0 rgba(0, 0, 0, 0);
  }
}
</style>