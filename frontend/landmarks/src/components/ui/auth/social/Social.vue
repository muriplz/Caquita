<template>
  <div class="flex flex-col p-2 h-[calc(100vh-200px)] modal">
    <div class="flex items-start gap-2 justify-between">
      <UserAvatar/>
      <div class="flex flex-col items-start my-3 h-full">
        <h2 class="text-xl mb-auto">{{Store.getUser()?.username}}</h2>
        <p class="text-xs text-gray-500 leading-relaxed mb-6">{{ formatDate(Store.getUser()?.creation) }}</p>
      </div>
    </div>

    <div class="relative" ref="tabsContainer">
      <div class="flex relative" ref="tabsNav">
        <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="selectTab(tab.id)"
            :class="{ 'font-bold': currentTab === tab.id }"
            class="flex-1 py-3 px-2 text-xs z-10 relative">
          {{ tab.name }}
        </button>
        <div
            class="absolute border-2 z-0"
            :style="borderStyle"></div>
      </div>
    </div>

    <div class="relative overflow-hidden flex-1">
      <transition :name="transitionName" mode="out-in" :duration="{ enter: 100, leave: 100 }">
        <div :key="currentTab" class="py-2.5 px-0 h-full w-full">
          <div v-if="currentTab === 'profile'">
            <Profile :user="Store.getUser()"/>
          </div>

          <div v-if="currentTab === 'friends'">
            <Friends/>
          </div>

          <div v-if="currentTab === 'requests'">
            <Requests/>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, computed, nextTick, onMounted, onUnmounted, watch } from "vue";
import FriendshipApi from "./js/FriendshipApi.js";
import Store from "@/js/Store.js";
import Profile from "../Profile.vue";
import Requests from "./Requests.vue";
import UserAvatar from "../UserAvatar.vue";

const friends = ref([]);
const pendingRequests = ref([]);
const currentTab = ref('profile');
const previousTab = ref('profile');
const transitionName = ref('slide-right');
const friendId = ref('');
const requestStatus = ref('');
const yourId = ref('');
const borderWidth = ref(0);
const borderLeft = ref(0);
const borderHeight = ref(0);
const borderTop = ref(0);
const tabsNav = ref(null);

const tabs = [
  { id: 'profile', name: 'Profile' },
  { id: 'friends', name: 'Friends' },
  { id: 'requests', name: 'Requests' }
];

const borderStyle = computed(() => {
  return {
    width: `${borderWidth.value}px`,
    height: `${borderHeight.value}px`,
    transform: `translate(${borderLeft.value}px, ${borderTop.value}px)`
  };
});

function selectTab(tabId) {
  previousTab.value = currentTab.value;
  currentTab.value = tabId;

  // Get indexes for animation direction
  const currentIndex = tabs.findIndex(tab => tab.id === previousTab.value);
  const newIndex = tabs.findIndex(tab => tab.id === tabId);
  transitionName.value = newIndex > currentIndex ? 'slide-right' : 'slide-left';
}

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

function updateBorderPosition() {
  if (!tabsNav.value) return;

  const activeTab = tabsNav.value.querySelector('button.font-bold');
  if (activeTab) {
    borderWidth.value = activeTab.offsetWidth;
    borderHeight.value = activeTab.offsetHeight;
    borderLeft.value = activeTab.offsetLeft;
    borderTop.value = activeTab.offsetTop;
  }
}

// Watch for tab changes to update border
watch(currentTab, () => {
  nextTick(() => {
    updateBorderPosition();
  });
});

onBeforeMount(async () => {
  await loadData();

  const userData = localStorage.getItem('userData');
  if (userData) {
    yourId.value = JSON.parse(userData).id;
  }
});

onMounted(() => {
  updateBorderPosition();

  window.addEventListener('resize', updateBorderPosition);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateBorderPosition);
});

async function loadData() {
  try {
    friends.value = await FriendshipApi.getFriends();
    pendingRequests.value = await FriendshipApi.getPendingRequests();
  } catch (error) {
    console.error('Failed to load data:', error);
  }
}
</script>

<style scoped>
.slide-right-enter-active,
.slide-right-leave-active,
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.1s ease, opacity 0.1s ease;
  position: absolute;
  width: 100%;
}

.slide-right-enter-from {
  transform: translateX(50px);
  opacity: 0;
}

.slide-right-leave-to {
  transform: translateX(-50px);
  opacity: 0;
}

.slide-left-enter-from {
  transform: translateX(-50px);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(50px);
  opacity: 0;
}
</style>