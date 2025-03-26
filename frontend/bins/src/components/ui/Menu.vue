<template>
  <div>
    <div v-if="isMenuOpen" class="settings-container">
      <a href="javascript:void(0)"
         class="settings-button"
         :class="{ 'spin': isSettingsSpinning }"
         @click="handleSettingsClick">
        <motion.div
            :animate="{ rotate: settingsRotation }"
            :initial="{ rotate: 0 }"
            :transition="{ duration: 0.3 }"
            style="transform-origin: center center; display: flex; justify-content: center; align-items: center;"
        >
          <img src="/images/ui/settings.png" alt="Settings" class="settings-icon" />
        </motion.div>
      </a>

      <a v-if="isAdmin"
         href="javascript:void(0)"
         class="admin-button"
         @click="showContributing">
        <img src="/images/ui/plus_button.png" class="admin-icon" alt=""/>
      </a>
    </div>

    <div v-if="isMenuOpen" class="profile-container">
      <a href="javascript:void(0)" class="profile-button" @click="toggleProfileMenu">
        <img :src="profileImageSrc" alt="Profile" class="profile-icon" />
      </a>
      <ProfileDropdown
          :isVisible="isProfileMenuOpen"
          @showLogin="showComponent('login')"
          @showRegister="showComponent('register')"
          @showProfile="showComponent('profile')"
          @logout="handleLogout"
          @close="isProfileMenuOpen = false"
      />
    </div>

    <a href="javascript:void(0)"
       class="menu-toggle-button"
       :class="menuButtonClass"
       @click="handleMenuButtonClick">
      <span>{{ menuButtonText }}</span>
    </a>

    <Transition name="menu-fade">
      <div v-if="isMenuOpen" class="fullscreen-menu" @click.self="handleBackgroundClick">
        <TransitionGroup
            v-if="!activeComponent"
            name="menu-buttons"
            tag="div"
            class="menu-container"
            appear>
          <MenuButton
              v-for="button in availableMenuButtons"
              :key="button.key"
              :label="button.label"
              :transitionDelay="button.delay"
              @click="showComponent(button.component)"
          />
        </TransitionGroup>

        <Transition name="component-slide">
          <div v-if="activeComponent"
               class="component-container"
               :class="{ 'centered-container': componentConfig[activeComponent]?.centered }"
               @click.self="closeComponent">
            <component :is="getComponentByName(activeComponent)" @close="closeComponent" @openMaterial="showMaterial" />
          </div>
        </Transition>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import {computed, ref, watch, markRaw, shallowRef} from 'vue'
import Inventory from "./inventory/Inventory.vue"
import AuthService from "@/js/auth/authService.js"
import ProfileDropdown from "@/components/ui/auth/ProfileDropdown.vue"
import Login from "@/components/ui/auth/Login.vue"
import Register from "@/components/ui/auth/Register.vue"
import Profile from "@/components/ui/auth/Profile.vue"
import Settings from "@/components/ui/settings/Settings.vue"
import Store from "@/js/Store.js"
import About from "@/components/ui/About.vue"
import Contributing from "@/components/ui/Contributing.vue"
import Cans from "@/components/ui/contributing/Cans.vue"
import Plastic from "@/components/ui/contributing/Plastic.vue"
import Cardboard from "@/components/ui/contributing/Cardboard.vue"
import Glass from "@/components/ui/contributing/Glass.vue"
import MenuButton from "@/components/ui/MenuButton.vue"
import {motion} from 'motion-v'

// State
const isMenuOpen = ref(false)
const activeComponent = ref(null)
const previousComponent = ref(null)
const isProfileMenuOpen = ref(false)
const isUserLoggedIn = ref(Store.getUser() !== null)
const isSettingsSpinning = ref(false)
const isAdmin = computed(() => Store.getUser()?.trust === "ADMINISTRATOR")
const settingsRotation = ref(0)
let spinTimeout;

// Component registry
const componentConfig = {
  inventory: {component: markRaw(Inventory), requiresAuth: true, centered: false},
  vitrine: {component: null, requiresAuth: true, centered: false},
  noidea: {component: null, requiresAuth: false, centered: false},
  settings: {component: markRaw(Settings), requiresAuth: false, centered: true},
  about: {component: markRaw(About), requiresAuth: false, centered: false},
  contributing: {component: markRaw(Contributing), requiresAuth: false, centered: true},
  cans: {component: markRaw(Cans), requiresAuth: false, centered: true},
  plastic: {component: markRaw(Plastic), requiresAuth: false, centered: true},
  cardboard: {component: markRaw(Cardboard), requiresAuth: false, centered: true},
  glass: {component: markRaw(Glass), requiresAuth: false, centered: true},
  login: {component: markRaw(Login), requiresAuth: false, centered: false},
  register: {component: markRaw(Register), requiresAuth: false, centered: false},
  profile: {component: markRaw(Profile), requiresAuth: true, centered: false}
}

// Computed
const menuButtonText = computed(() => {
  if (activeComponent.value) return 'Back'
  if (isMenuOpen.value) return 'Close'
  return 'Menu'
})

const menuButtonClass = computed(() => {
  if (activeComponent.value) return 'back'
  if (isMenuOpen.value) return 'open'
  return 'closed'
})

const profileImageSrc = computed(() => {
  return isUserLoggedIn.value
      ? '/images/ui/profile_logged.png'
      : '/images/ui/profile.png'
})

const availableMenuButtons = computed(() => {
  const buttons = []
  let delay = 0.05

  if (isUserLoggedIn.value) {
    buttons.push({key: 'inventory', label: 'Inventory', component: 'inventory', delay: `${delay}s`})
    delay += 0.05
    buttons.push({key: 'vitrine', label: 'Vitrine', component: 'vitrine', delay: `${delay}s`})
    delay += 0.05
  }

  buttons.push({key: 'noidea', label: 'NoIdea', component: 'noidea', delay: `${delay}s`})

  return buttons
})

// Methods
function checkUserStatus() {
  isUserLoggedIn.value = Store.getUser() !== null
}

function getComponentByName(name) {
  return componentConfig[name]?.component || shallowRef(null)
}

function showComponent(name) {
  if (componentConfig[name]?.requiresAuth && !isUserLoggedIn.value) {
    activeComponent.value = 'login'
    return
  }

  activeComponent.value = name
  isProfileMenuOpen.value = false
}

function handleMenuButtonClick() {
  if (activeComponent.value) {
    closeComponent()
  } else {
    toggleMenu()
  }
}

function toggleMenu() {
  isMenuOpen.value = !isMenuOpen.value
}

function handleBackgroundClick() {
  if (activeComponent.value) {
    closeComponent()
  } else {
    isMenuOpen.value = false
  }
}

function toggleProfileMenu() {
  isProfileMenuOpen.value = !isProfileMenuOpen.value
}

function showSettings() {
  if (activeComponent.value === 'settings') {
    if (previousComponent.value) {
      activeComponent.value = previousComponent.value
      previousComponent.value = null
    } else {
      closeComponent()
    }
  } else {
    if (activeComponent.value) {
      previousComponent.value = activeComponent.value
    }
    activeComponent.value = 'settings'
  }
}

function showContributing() {
  showComponent('contributing')
}

function showMaterial(type) {
  showComponent(type)
}

function handleSettingsClick() {
  // Increase rotation by 90 degrees
  settingsRotation.value += 90

  // Show settings
  showSettings()
}

function closeComponent() {
  activeComponent.value = null
  previousComponent.value = null
}

function closeAuth() {
  activeComponent.value = null
  checkUserStatus()
}

function handleLogout() {
  AuthService.logout()
  isProfileMenuOpen.value = false
  activeComponent.value = null
  previousComponent.value = null
  checkUserStatus()
}

watch(isMenuOpen, (newVal) => {
  if (newVal === true) {
    checkUserStatus();
    // Reset rotation when menu opens
    settingsRotation.value = 0;
  }
});
</script>

<style scoped>
.settings-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 300;
  display: flex;
  gap: 10px;
}

.menu-toggle-button {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  padding: 12px 24px;
  border-radius: 24px;
  cursor: pointer;
  z-index: 300;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  text-decoration: none;
  display: inline-block;
  transition: background-color 0.2s, transform 0.2s;
}

.menu-toggle-button.closed {
  background-color: #333;
}

.menu-toggle-button.open {
  background-color: #f44336;
}

.menu-toggle-button.back {
  background-color: #42a5f5;
}

.menu-toggle-button:hover {
  text-decoration: none;
  color: white;
}

.menu-button {
  display: block;
  padding: 16px 32px;
  background-color: white;
  color: #333;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  width: 200px;
  text-align: center;
  text-decoration: none;
}

.menu-button:hover {
  background-color: #f0f0f0;
  text-decoration: none;
  color: #333;
}

.settings-button, .profile-button {
  color: white;
  cursor: pointer;
  text-decoration: none;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: opacity 0.2s;
  position: relative;
  width: 48px;
  height: 48px;
}

.settings-button:hover, .profile-button:hover {
  opacity: 0.8;
}

.admin-button img {
  width: 32px;
  height: 32px;
  margin-top: 8px;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}

.profile-icon {
  width: 52px;
  height: 52px;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
  will-change: transform;
  backface-visibility: hidden;
}

.settings-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 300;
  display: flex;
  gap: 10px;
}

.settings-button, .profile-button {
  color: white;
  cursor: pointer;
  text-decoration: none;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: opacity 0.2s;
  position: relative;
  width: 48px;
  height: 48px;
}

.settings-button:hover, .profile-button:hover {
  opacity: 0.8;
}

.settings-icon {
  width: 46px;
  height: 46px;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
  will-change: transform;
  backface-visibility: hidden;
}

.profile-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 300;
}

.fullscreen-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: flex;
  justify-content: center;
  align-items: center;
}

.menu-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.component-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 50;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  pointer-events: auto;
  cursor: pointer;
}

.centered-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.menu-fade-enter-active {
  transition: opacity 0.3s ease;
}

.menu-fade-leave-active {
  transition: opacity 0.2s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
}

.menu-buttons-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.menu-buttons-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 1, 1);
}

.menu-buttons-enter-from {
  opacity: 0;
  transform: translateY(60px);
}

.menu-buttons-leave-to {
  opacity: 0;
  transform: translateY(60px);
}

.menu-buttons-move {
  transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.component-slide-enter-active {
  animation: slide-in 0.5s;
}

.component-slide-leave-active {
  animation: slide-out 0.3s;
}

@keyframes slide-in {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slide-out {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(-100%);
    opacity: 0;
  }
}
</style>