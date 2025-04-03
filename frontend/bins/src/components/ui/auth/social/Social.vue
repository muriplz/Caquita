<template>
  <div class="social-card modal">

    <div class="user-info">
      <div class="user-name">
        <h3>@{{Store.getUser().username}}</h3>
      </div>
      <LevelBar />
    </div>

    <div class="tabs-container">
      <div class="tabs">
        <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="selectTab(tab.id)"
            :class="{ active: currentTab === tab.id }">
          {{ tab.name }}
        </button>
        <div class="active-tab-highlight" :style="borderStyle"></div>
      </div>
    </div>

    <div class="tab-container">
      <transition :name="transitionName" mode="out-in" :duration="{ enter: 100, leave: 100 }">
        <div :key="currentTab" class="tab-content">
          <div v-if="currentTab === 'profile'">
            <Profile :user="Store.getUser()"/>
          </div>

          <div v-if="currentTab === 'friends'">
            <h3>My Friends</h3>
            <div v-if="friends && friends.length > 0" class="friends-list">
              <div v-for="friend in friends" :key="friend.id" class="friend-item">
                {{ friend.username }}
                <button @click="removeFriend(friend.id)" class="remove-btn">Remove</button>
              </div>
            </div>
            <div v-else class="empty-state">No friends yet</div>
          </div>

          <div v-if="currentTab === 'pending'">
            <Requests/>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, computed, nextTick, onMounted, onUnmounted } from "vue";
import FriendshipApi from "./js/FriendshipApi.js";
import LevelBar from "../LevelBar.vue";
import Store from "@/js/Store.js";
import Profile from "../Profile.vue";
import Requests from "./Requests.vue";

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

const tabs = [
  { id: 'profile', name: 'Profile' },
  { id: 'friends', name: 'Friends' },
  { id: 'pending', name: 'Requests' }
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

  nextTick(() => {
    updateBorderPosition();
  });
}

function updateBorderPosition() {
  const activeTab = document.querySelector('.tabs button.active');
  if (activeTab) {
    borderWidth.value = activeTab.offsetWidth;
    borderHeight.value = activeTab.offsetHeight;
    borderLeft.value = activeTab.offsetLeft;
    borderTop.value = activeTab.offsetTop;
  }
}

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

async function sendFriendRequest() {
  if (!friendId.value) return;

  try {
    await FriendshipApi.sendRequest(parseInt(friendId.value));
    requestStatus.value = 'Friend request sent!';
    friendId.value = '';
    setTimeout(() => requestStatus.value = '', 3000);
  } catch (error) {
    requestStatus.value = 'Failed to send request';
  }
}

async function respondToRequest(requesterId, action) {
  try {
    await FriendshipApi.respondToRequest(requesterId, action);
    await loadData();
  } catch (error) {
    console.error('Failed to respond to request:', error);
  }
}

async function removeFriend(friendId) {
  try {
    await FriendshipApi.removeFriend(friendId);
    await loadData();
  } catch (error) {
    console.error('Failed to remove friend:', error);
  }
}
</script>

<style scoped>
.social-card {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  padding: 8px;
  width: min(calc(100vw - 48px), 800px);
  height: calc(100vh - 300px);
}

.tabs-container {
  position: relative;
}

.tabs {
  display: flex;
  position: relative;
}

.tabs button {
  flex: 1;
  padding: 12px 8px;
  background: none;
  border: none;
  border-radius: 0;
  cursor: pointer;
  font: inherit;
  font-size: 0.8rem;
  white-space: nowrap;
  z-index: 1;
  position: relative;
}

.tabs button.active {
  font-weight: bold;
}

.active-tab-highlight {
  position: absolute;
  background-color: transparent;
  border: 2px solid #333;
  transition: transform 0.1s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 0;
}

.tab-container {
  position: relative;
  overflow: hidden;
  flex: 1;
}

.tab-content {
  padding: 10px 0;
  height: 100%;
  width: 100%;
}

.friend-item, .request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #eee;
}

.request-actions {
  display: flex;
  gap: 4px;
}

.friend-input {
  width: 100%;
  padding: 8px;
  margin-bottom: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.add-btn, .accept-btn, .reject-btn, .remove-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn {
  background: #42b983;
  color: white;
  width: 100%;
}

.accept-btn {
  background: #42b983;
  color: white;
}

.reject-btn, .remove-btn {
  background: #f56c6c;
  color: white;
}

.status-message {
  margin-top: 8px;
  text-align: center;
  color: #42b983;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 20px;
}

.user-name {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name p {
  margin: 0;
}

.user-info {
  align-items: center;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.slide-right-enter-active,
.slide-right-leave-active {
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

.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.1s ease, opacity 0.1s ease;
  position: absolute;
  width: 100%;
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