<template>
  <div>
    <div v-if="isMenuOpen" class="settings-container">
      <a href="javascript:void(0)"
         class="settings-button"
         :class="{ 'spin': isSettingsSpinning }"
         @click="showSettings">
        <img src="/images/ui/settings.png" alt="Settings" class="settings-icon" />
      </a>
    </div>

    <div v-if="isMenuOpen" class="profile-container">
      <a href="javascript:void(0)" class="profile-button" @click="toggleProfileMenu">
        <img v-if="isUserLoggedIn" src="/images/ui/profile_logged.png" alt="Profile" class="profile-icon" />
        <img v-else src="/images/ui/profile.png" alt="Profile" class="profile-icon" />
      </a>
      <ProfileDropdown
          :isVisible="isProfileMenuOpen"
          @showLogin="showLogin"
          @showRegister="showRegister"
          @showProfile="showProfile"
          @logout="handleLogout"
          @close="isProfileMenuOpen = false"
      />
    </div>

    <a href="javascript:void(0)"
       class="menu-toggle-button"
       :class="{
         'closed': !isMenuOpen && !activeComponent,
         'open': isMenuOpen && !activeComponent,
         'back': activeComponent
       }"
       @click="handleMenuButtonClick">
      <span>{{ menuButtonText }}</span>
    </a>

    <Transition name="menu-fade">
      <div v-if="isMenuOpen" class="fullscreen-menu" @click.self="handleBackgroundClick">
        <TransitionGroup
            name="menu-buttons"
            tag="div"
            class="menu-container"
            v-if="!activeComponent"
            appear>
          <a v-if="isUserLoggedIn" key="inventory" href="javascript:void(0)" class="menu-button" style="transition-delay: 0.05s" @click="showInventory">Inventory</a>
          <a v-if="isUserLoggedIn" key="vitrine" href="javascript:void(0)" class="menu-button" style="transition-delay: 0.1s" @click="showVitrine">Vitrine</a>
          <a key="noidea" href="javascript:void(0)" class="menu-button" :style="{transitionDelay: isUserLoggedIn ? '0.15s' : '0.05s'}" @click="showNoIdea">NoIdea</a>
        </TransitionGroup>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'inventory'" class="component-container" @click.self="closeComponent">
            <inventory :cell-size="60" />
          </div>
        </Transition>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'vitrine'" class="component-container" @click.self="closeComponent">
            <h2>Vitrine Component</h2>
          </div>
        </Transition>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'noidea'" class="component-container" @click.self="closeComponent">
            <h2>NoIdea Component</h2>
          </div>
        </Transition>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'settings'" class="component-container centered-container" @click.self="closeComponent">
            <Settings @close="closeComponent" />
          </div>
        </Transition>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'about'" class="component-container" @click.self="closeComponent">
            <About @close="closeComponent" />
          </div>
        </Transition>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'login'" class="component-container" @click.self="closeComponent">
            <Login
                @success="closeAuth"
                @cancel="closeAuth"
                @showRegister="handleShowRegister"
                @showAbout="handleShowAbout"
            />
          </div>
        </Transition>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'register'" class="component-container" @click.self="closeComponent">
            <Register
                @success="closeAuth"
                @cancel="closeAuth"
                @showLogin="handleShowLogin"
                @showAbout="handleShowAbout"
            />
          </div>
        </Transition>

        <Transition name="component-slide">
          <div v-if="activeComponent === 'profile'" class="component-container" @click.self="closeComponent">
            <Profile
                @close="closeAuth"
            />
          </div>
        </Transition>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import Inventory from "./inventory/Inventory.vue"
import AuthService from "@/js/auth/authService.js"
import ProfileDropdown from "@/components/ui/auth/ProfileDropdown.vue"
import Login from "@/components/ui/auth/Login.vue"
import Register from "@/components/ui/auth/Register.vue"
import Profile from "@/components/ui/auth/Profile.vue"
import Settings from "@/components/ui/settings/Settings.vue"
import Store from "@/js/Store.js"
import About from "@/components/ui/About.vue"

const isMenuOpen = ref(false)
const activeComponent = ref(null)
const previousComponent = ref(null)
const isProfileMenuOpen = ref(false)
const isUserLoggedIn = ref(Store.getUser() !== null)
const isSettingsSpinning = ref(false)

function checkUserStatus() {
  isUserLoggedIn.value = Store.getUser() !== null
}

watch(isMenuOpen, (newVal) => {
  if (newVal === true) {
    checkUserStatus()
  }
})

const menuButtonText = computed(() => {
  if (activeComponent.value) return 'Back'
  if (isMenuOpen.value) return 'Close'
  return 'Menu'
})

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

function showInventory() {
  activeComponent.value = 'inventory'
}

function showVitrine() {
  activeComponent.value = 'vitrine'
}

function showNoIdea() {
  activeComponent.value = 'noidea'
}

function showSettings() {
  if (spinTimeout) {
    clearTimeout(spinTimeout);
  }

  triggerSpinAnimation();

  if (activeComponent.value === 'settings') {
    if (previousComponent.value) {
      activeComponent.value = previousComponent.value;
      previousComponent.value = null;
    } else {
      closeComponent();
    }
  } else {
    if (activeComponent.value) {
      previousComponent.value = activeComponent.value;
    }
    activeComponent.value = 'settings';
  }
}

let spinTimeout;

function triggerSpinAnimation() {
  isSettingsSpinning.value = true;
  spinTimeout = setTimeout(() => {
    isSettingsSpinning.value = false;
    spinTimeout = null;
  }, 500);
}

function showAbout() {
  activeComponent.value = 'about'
}

function showLogin() {
  activeComponent.value = 'login'
  isProfileMenuOpen.value = false
}

function showRegister() {
  activeComponent.value = 'register'
  isProfileMenuOpen.value = false
}

function showProfile() {
  activeComponent.value = 'profile'
  isProfileMenuOpen.value = false
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

function handleShowRegister() {
  activeComponent.value = 'register'
}

function handleShowLogin() {
  activeComponent.value = 'login'
}

function handleShowAbout() {
  activeComponent.value = 'about'
}
</script>

<style scoped>
.settings-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 300;
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

.profile-icon {
  width: 52px;
  height: 52px;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
  will-change: transform;
  backface-visibility: hidden;
}

.settings-icon {
  width: 46px;
  height: 46px;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
  will-change: transform;
  backface-visibility: hidden;
}

.settings-button.spin .settings-icon {
  animation: spin 0.5s linear;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(90deg);
  }
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
  background-color: rgba(0, 0, 0, 0.7);
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