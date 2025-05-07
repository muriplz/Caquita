<template>
  <div class="relative w-full pt-4 min-h-[60px]">
    <div class="absolute flex items-center justify-center -top-2.5 right-2.5 w-8 h-9 rounded-b-full shadow-md z-10 border-b-2 border-r-2 border-l-2 border-gray-800 bg-white active:scale-95"
         @click="toggleAddFriend">
      <img src="/images/ui/add_friend.png" class=""/>
    </div>

    <div class="relative w-full min-h-full">
      <transition name="slide-from-top">
        <div v-if="showAddFriend" class="absolute top-0 left-0 w-full z-[1]">
          <div class="relative flex w-full text-xs mt-4 px-2">
            <input type="text" v-model="friendUsername" placeholder="Add a friend" class="w-full border-2 border-gray-800 p-1.5" />
            <button class="bg-gray-400 border-2 border-gray-800 text-xs my-0.5 ml-1.5 cursor-pointer" @click="addFriend">Add</button>
          </div>
        </div>
      </transition>

      <div class="relative w-full" :style="requestsStyle">
        <div v-if="requests && requests.length > 0" class="p-2">
          <div v-for="request in requests" :key="request.id" class="flex justify-between items-center p-1.5 border-b border-gray-200 text-base">
            {{ request.username }}
            <div class="flex gap-1">
              <button @click="respondToRequest(request.id, 'ACCEPTED')" class="py-0.5 px-1 rounded cursor-pointer text-xs bg-green-500 text-white">Accept</button>
              <button @click="respondToRequest(request.id, 'REJECTED')" class="py-0.5 px-1 rounded cursor-pointer text-xs bg-red-500 text-white">Reject</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onBeforeMount, ref, computed} from "vue";
import FriendshipApi from "./js/FriendshipApi.js";

const requests = ref([]);
const showAddFriend = ref(false);
const friendUsername = ref("");
const inputHeight = ref(80);

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