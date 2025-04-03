<template>
  <div class="container">
    <div class="add-friend-trigger" @click="toggleAddFriend">
      <span class="plus-icon">+</span>
    </div>

    <div class="content-wrapper">
      <transition name="slide-from-top">
        <div v-if="showAddFriend" class="add-friend-panel">
          <div class="add-friend-input">
            <input type="text" v-model="friendUsername" placeholder="Add a friend" />
            <button class="add-friend-button" @click="addFriend">Add</button>
          </div>
        </div>
      </transition>

      <div class="pending-requests-wrapper" :style="requestsStyle">
        <div v-if="requests && requests.length > 0" class="pending-requests">
          <div v-for="request in requests" :key="request.id" class="request-item">
            {{ request.username }}
            <div class="request-actions">
              <button @click="respondToRequest(request.id, 'ACCEPTED')" class="accept-btn">Accept</button>
              <button @click="respondToRequest(request.id, 'REJECTED')" class="reject-btn">Reject</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, computed } from "vue";
import FriendshipApi from "./js/FriendshipApi.js";

const requests = ref([]);
const showAddFriend = ref(false);
const friendUsername = ref("");
const inputHeight = ref(80); // Approximate height of the add friend input section

// Computed style for the requests section to move in sync with input
const requestsStyle = computed(() => {
  if (showAddFriend.value) {
    return {
      transform: `translateY(${inputHeight.value}px)`,
      transition: 'transform 0.25s ease'
    };
  } else {
    return {
      transform: 'translateY(0)',
      transition: 'transform 0.25s ease'
    };
  }
});

onBeforeMount(async () => {
  requests.value = await FriendshipApi.getPendingRequests();
});

function toggleAddFriend() {
  showAddFriend.value = !showAddFriend.value;
}

async function addFriend() {
  if (!friendUsername.value) return;

  await FriendshipApi.sendRequest(friendUsername.value);
  friendUsername.value = "";
}

async function respondToRequest(requesterId, action) {
  try {
    await FriendshipApi.respondToRequest(requesterId, action);
    requests.value = await FriendshipApi.getPendingRequests();
  } catch (error) {
    console.error("Failed to respond to request:", error);
  }
}
</script>

<style scoped>
.container {
  position: relative;
  width: 100%;
  padding-top: 16px;
  min-height: 60px;
}

.content-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 100px;
  min-height: 100%;
}

.add-friend-trigger {
  position: absolute;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  top: -10px;
  right: 10px;
  width: 32px;
  height: 32px;
  border-bottom-right-radius: 50%;
  border-bottom-left-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  cursor: pointer;
  overflow: visible;
  z-index: 2;
}

.plus-icon {
  font-size: 20px;
  font-weight: bold;
}

.add-friend-panel {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1;
  background-color: white;
}

.add-friend-input {
  position: relative;
  display: flex;
  width: 100%;
  font-size: 0.7rem;
  margin-top: 16px;
  margin-bottom: 8px;
  padding: 0 8px;
}

.add-friend-input input {
  width: 100%;
  font: inherit;
  border: 2px solid #333;
  padding: 6px;
}

.add-friend-input button {
  background: #9f9f9f;
  border: 2px solid #333;
  font: inherit;
  font-size: 0.6rem;
  border-radius: 0;
  margin: 3px 0 3px 6px;
  cursor: pointer;
}

.pending-requests-wrapper {
  position: relative;
  width: 100%;
  transform: translateY(0);
}

.pending-requests {
  margin-top: 8px;
  padding: 12px 8px;
}

.request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px;
  border-bottom: 1px solid #eee;
  font-size: 0.7rem;
}

.request-actions {
  display: flex;
  gap: 4px;
}

.accept-btn, .reject-btn {
  padding: 2px 4px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.6rem;
}

.accept-btn {
  background: #42b983;
  color: white;
}

.reject-btn {
  background: #f56c6c;
  color: white;
}

/* Transitions */
.slide-from-top-enter-active,
.slide-from-top-leave-active {
  transition: transform 0.25s ease, opacity 0.3s ease;
  transform-origin: top center;
}

.slide-from-top-enter-from {
  transform: translateY(-100%);
  opacity: 0;
}

.slide-from-top-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}
</style>