
<script setup>
import {motion} from 'motion-v'
import {computed, markRaw, ref, onMounted, onUnmounted} from 'vue'
import Inventory from "./inventory/Inventory.vue"
import AuthService from "@/js/auth/AuthService.js"
import ProfileDropdown from "@/components/ui/auth/ProfileDropdown.vue"
import Profile from "@/components/ui/auth/Profile.vue"
import Settings from "@/components/ui/settings/Settings.vue"
import Store from "@/js/Store.js"
import About from "@/components/ui/About.vue"
import Contributing from "@/components/ui/Contributing.vue"
import MenuButton from "@/components/ui/MenuButton.vue"
import uiRouter from "./UIRouter.js"
import ItemInfoScreen from "@/components/ui/inventory/ItemInfoScreen.vue"
import CurrencyPanel from "@/components/ui/auth/CurrencyPanel.vue"
import Social from "./auth/social/Social.vue";

const isDevelopment = process.env.NODE_ENV === 'development' || true;

const isProfileMenuOpen = ref(false)
const settingsRotation = ref(0)

const screenComponents = {
  INVENTORY: markRaw(Inventory),
  SETTINGS: markRaw(Settings),
  ABOUT: markRaw(About),
  CONTRIBUTING: markRaw(Contributing),
  SOCIAL: markRaw(Social),
  ITEM_INFO: markRaw(ItemInfoScreen)
}

const centeredScreens = ['SETTINGS', 'CONTRIBUTING']

onMounted(() => {
  uiRouter.reset()
  settingsRotation.value = 0
  isProfileMenuOpen.value = false
})

onUnmounted(() => {
  uiRouter.reset()
})

const showMainMenu = computed(() => {
  return uiRouter.state.menuIsVisible &&
      !uiRouter.state.currentScreen &&
      !uiRouter.state.isAnimating;
})

const showScreens = computed(() => {
  return uiRouter.state.currentScreen !== null || uiRouter.state.isAnimating;
})

const showScreenContent = computed(() => {
  return uiRouter.state.currentScreen !== null || uiRouter.state.isAnimating;
})

const screenContentKey = computed(() => {
  if (uiRouter.state.isAnimating) {
    if (uiRouter.state.targetScreen === 'MAIN_MENU') {
      return `to-main-menu-${Date.now()}`;
    }
    if (uiRouter.state.previousScreen === null) {
      return `from-main-menu-${Date.now()}`;
    }
  }

  return uiRouter.state.currentScreen || 'placeholder';
})

const isAdmin = computed(() => Store.state.user?.trust === "ADMINISTRATOR")

const menuButtonText = computed(() => {
  if (uiRouter.state.currentScreen || uiRouter.state.isAnimating) return 'Back'
  if (uiRouter.state.isMenuOpen) return 'Close'
  return 'Menu'
})

const menuButtonClass = computed(() => {
  if (uiRouter.state.currentScreen || uiRouter.state.isAnimating) return 'back'
  if (uiRouter.state.isMenuOpen) return 'open'
  return 'closed'
})

const isCenteredScreen = computed(() => {
  if (uiRouter.state.isAnimating && uiRouter.state.targetScreen) {
    return centeredScreens.includes(uiRouter.state.targetScreen);
  }
  return centeredScreens.includes(uiRouter.state.currentScreen);
})

const transitionName = computed(() => {
  return uiRouter.state.animationDirection === 'forward'
      ? 'component-slide-forward'
      : 'component-slide-backward'
})

const currentComponent = computed(() => {
  const screenName = uiRouter.state.currentScreen;

  if (uiRouter.state.isAnimating && uiRouter.state.targetScreen === 'MAIN_MENU') {
    return null;
  }

  if (screenName === '_PLACEHOLDER_') {
    return null;
  }

  if (!screenName) return null;

  const component = screenComponents[screenName];
  if (!component) {
    if (isDevelopment) {
      console.warn(`Component not found for screen: ${screenName}`);
    }
    return null;
  }

  return component;
})

const availableMenuButtons = computed(() => {
  const buttons = [
    {key: 'inventory', label: 'Inventory', screen: 'INVENTORY'},
    {key: 'hauler', label: 'Hauler', screen: 'HAULER'},
    {key: 'noidea', label: 'NoIdea', screen: 'NOIDEA'},
    {key: 'about', label: 'About', screen: 'ABOUT'}
  ];

  return buttons.map((button, index) => {
    const baseDelay = 0.03;
    const staggerDelay = 0.04;
    return {
      ...button,
      delay: `${baseDelay + (Math.pow(index, 0.8) * staggerDelay)}s`
    };
  });
})

function navigateTo(screenName) {
  if (uiRouter.state.isAnimating) return;

  isProfileMenuOpen.value = false;

  if (screenName === 'SETTINGS') {
    settingsRotation.value += 90;
  }

  if (screenName !== 'MAIN_MENU' && screenName && !screenComponents[screenName]) {
    if (isDevelopment) {
      console.warn(`Cannot navigate to screen "${screenName}" - component not registered`);
    }
    return;
  }

  uiRouter.navigate(screenName);
}

function handleMenuButtonClick() {
  if (uiRouter.state.isAnimating) return;

  if (uiRouter.state.currentScreen) {
    goBack();
  } else {
    toggleMenu();
  }
}

function toggleMenu() {
  if (uiRouter.state.isMenuOpen) {
    uiRouter.closeMenu();
  } else {
    uiRouter.openMenu();
    settingsRotation.value = 0;
  }
}

function handleBackgroundClick() {
  if (uiRouter.state.isAnimating) return;

  if (uiRouter.state.currentScreen) {
    goBack();
  } else {
    uiRouter.closeMenu();
  }
}

function goBack() {
  if (uiRouter.state.isAnimating) return;
  uiRouter.goBack();
}

function toggleProfileMenu() {
  if (uiRouter.state.isAnimating) return;
  isProfileMenuOpen.value = !isProfileMenuOpen.value;
}

function handleSettingsClick() {
  if (uiRouter.state.isAnimating) return;

  settingsRotation.value += 90;
  uiRouter.toggleSettings();
}

function showContributing() {
  if (uiRouter.state.isAnimating) return;
  navigateTo('CONTRIBUTING');
}

function showMaterial(type) {
  if (uiRouter.state.isAnimating) return;

  const materialTypeMap = {
    'cans': 'CANS',
    'plastic': 'PLASTIC',
    'cardboard': 'CARDBOARD',
    'glass': 'GLASS'
  };

  if (materialTypeMap[type]) {
    navigateTo(materialTypeMap[type]);
  }
}

function handleLogout() {
  AuthService.logout();
  isProfileMenuOpen.value = false;
  uiRouter.closeMenu();
}
</script>

<template>
  <div>
    <Transition name="slide-from-top">
      <div v-if="uiRouter.state.isMenuOpen" class="settings-container">
        <Transition name="fade-scale">
          <a v-if="uiRouter.state.globalElements.showSettingsButton"
             href="javascript:void(0)"
             class="settings-button"
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
        </Transition>

        <Transition name="fade-scale">
          <a v-if="uiRouter.state.globalElements.showPlusButton"
             href="javascript:void(0)"
             class="admin-button"
             @click="showContributing">
            <img src="/images/ui/plus_button.png" class="admin-icon" alt=""/>
          </a>
        </Transition>
      </div>
    </Transition>

    <Transition name="slide-from-top">
      <div v-if="uiRouter.state.isMenuOpen && uiRouter.state.globalElements.showCurrencyPanel" class="currency-container">
        <CurrencyPanel />
      </div>
    </Transition>

    <Transition name="slide-from-top">
      <div v-if="uiRouter.state.isMenuOpen && uiRouter.state.globalElements.showProfileButton" class="profile-container">
        <a href="javascript:void(0)" class="profile-button" @click="toggleProfileMenu">
          <img src="/images/ui/profile_logged.png" alt="Profile" class="profile-icon" />
        </a>
        <ProfileDropdown
            :isVisible="isProfileMenuOpen"
            @showSocial="navigateTo('SOCIAL')"
            @logout="handleLogout"
            @close="isProfileMenuOpen = false"
        />
      </div>
    </Transition>

    <a href="javascript:void(0)"
       class="menu-toggle-button"
       :class="menuButtonClass"
       @click="handleMenuButtonClick">
      <span>{{ menuButtonText }}</span>
    </a>

    <Transition name="menu-fade">
      <div v-if="uiRouter.state.isMenuOpen" class="fullscreen-menu" @click.self="handleBackgroundClick">
        <TransitionGroup
            v-if="showMainMenu"
            name="menu-buttons"
            tag="div"
            class="menu-container"
            appear>
          <MenuButton
              v-for="button in availableMenuButtons"
              :key="button.key"
              :label="button.label"
              :transitionDelay="button.delay"
              @click="navigateTo(button.screen)"
          />
        </TransitionGroup>

        <div v-if="showScreens" class="screens-container">
          <Transition :name="transitionName">
            <div v-if="showScreenContent"
                 :key="screenContentKey"
                 class="component-container"
                 :class="{ 'centered-container': isCenteredScreen }"
                 @click.self="goBack">
              <component
                  v-if="currentComponent"
                  :is="currentComponent"
                  @close="goBack"
                  @openMaterial="showMaterial"
              />
              <div v-else class="empty-container"></div>
            </div>
          </Transition>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.settings-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 300;
  display: flex;
  gap: 10px;
}

.slide-from-top-enter-active {
  transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
}

.slide-from-top-leave-active {
  transition: all 0.2s cubic-bezier(0.5, 0, 0.75, 0);
}

.slide-from-top-enter-from {
  transform: translateY(-30px);
  opacity: 0;
}

.slide-from-top-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

.fade-scale-enter-active {
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
  transition-delay: calc(var(--index, 0) * 0.05s);
}

.fade-scale-leave-active {
  transition: all 0.15s cubic-bezier(0.5, 0, 0.75, 0);
}

.fade-scale-enter-from {
  transform: scale(0.8);
  opacity: 0;
}

.fade-scale-leave-to {
  transform: scale(0.9);
  opacity: 0;
}

/* Rest of the existing styles */
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
  border: 4px solid #333;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  text-decoration: none;
  display: inline-block;
  transition: background-color 0.2s;
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
  user-select: none;
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

.currency-container {
  position: fixed;
  top: 28px;
  right: 90px;
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
  overflow: hidden;
}

.menu-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 201;
}

.screens-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 202;
  overflow: hidden;
  will-change: transform;
}

.component-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  pointer-events: auto;
}

.empty-container {
  width: 100%;
  height: 100%;
}

.centered-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.menu-fade-enter-active {
  transition: opacity 0.2s ease;
}

.menu-fade-leave-active {
  transition: opacity 0.15s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
}

.menu-buttons-enter-active {
  transition: opacity 0.25s ease, transform 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
  will-change: opacity, transform;
  backface-visibility: hidden;
}

.menu-buttons-leave-active {
  transition: opacity 0.15s ease-out, transform 0.2s cubic-bezier(0.4, 0, 0.6, 1);
  will-change: opacity, transform;
  backface-visibility: hidden;
}

.menu-buttons-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.menu-buttons-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.menu-buttons-move {
  transition: transform 0.25s cubic-bezier(0.2, 0.8, 0.2, 1);
  will-change: transform;
}

.component-slide-forward-enter-active {
  animation: slide-in-forward 0.2s cubic-bezier(0.25, 1, 0.5, 1);
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 10;
}

.component-slide-forward-leave-active {
  animation: slide-out-forward 0.2s cubic-bezier(0.5, 0, 0.75, 0);
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 5;
}

@keyframes slide-in-forward {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slide-out-forward {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(-100%);
    opacity: 0;
  }
}

.component-slide-backward-enter-active {
  animation: slide-in-backward 0.2s cubic-bezier(0.25, 1, 0.5, 1);
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 10;
}

.component-slide-backward-leave-active {
  animation: slide-out-backward 0.2s cubic-bezier(0.5, 0, 0.75, 0);
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 5;
}

@keyframes slide-in-backward {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slide-out-backward {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .menu-buttons-enter-active,
  .menu-buttons-leave-active,
  .menu-buttons-move,
  .slide-from-top-enter-active,
  .slide-from-top-leave-active,
  .fade-scale-enter-active,
  .fade-scale-leave-active {
    transition-duration: 0.1s;
  }
}
</style>