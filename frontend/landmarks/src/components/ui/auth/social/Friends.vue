<template>
  <div v-if="friends && friends.length > 0" class="w-full">
    <div v-for="friend in friends" :key="friend.id" class="flex justify-between items-center py-2">
      <div class="flex items-center gap-2.5">
        <img class="w-[72px]" style="image-rendering: pixelated;" :src="`/images/ui/avatar/${avatars[friend.id]}.png`" />
        <div class="flex flex-col items-start gap-0.5">
          <div class="flex items-center gap-1">
            <h3 class="m-0 pr-0.5 text-lg">{{ friend.username }}</h3>
          </div>
          <p
              class="m-0 text-sm text-gray-500"
              :class="{ 'text-green-500 font-bold': connectionStatus(friend).isOnline }"
          >
            {{ connectionStatus(friend).text }}
          </p>
        </div>
        <FriendLevelBar :friend="friend"/>
      </div>
      <img class="w-6 h-6 cursor-pointer" style="image-rendering: pixelated;" @click="removeFriend(friend.id)" src="/images/ui/remove_friend.png" alt="Remove Friend" />
    </div>
  </div>
  <div v-else class="text-center p-5 text-gray-500">No friends yet</div>
</template>

<script setup>
import {computed, onBeforeMount, ref} from "vue";
import FriendshipApi from "./js/FriendshipApi.js";
import FriendLevelBar from "./FriendLevelBar.vue";

const friends = ref([]);
const avatars = ref([]);

async function removeFriend(friendId) {
  await FriendshipApi.removeFriend(friendId);
  await fetchData()
}

async function fetchData() {
  friends.value = await FriendshipApi.getFriends()
  avatars.value = await FriendshipApi.getAvatars()
}

onBeforeMount(async () => {
  await fetchData()
})

function getUserLevel(friend) {
  if (!friend || !friend.level) return '?';

  // Handle JSONB format from PostgreSQL
  if (friend.level.type === 'jsonb' && typeof friend.level.value === 'string') {
    try {
      const levelData = JSON.parse(friend.level.value);
      return levelData.level;
    } catch (e) {
      console.error("Error parsing level JSON:", e);
      return '?';
    }
  }

  return '?';
}

function connectionStatus(friend) {
  if (!friend.connection) return 'Never connected';

  const now = new Date();
  const connectionTime = new Date(friend.connection);
  const diffMs = now - connectionTime;

  // Less than 30 minutes
  if (diffMs < 30 * 60 * 1000) {
    return {
      text: 'ONLINE',
      isOnline: true
    };
  }

  const diffMinutes = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);
  const diffMonths = Math.floor(diffDays / 30);
  const diffYears = Math.floor(diffMonths / 12);

  if (diffYears > 0) {
    return {
      text: `${diffYears} y ago`,
      isOnline: false
    };
  } else if (diffMonths > 0) {
    return {
      text: `${diffMonths} m ago`,
      isOnline: false
    };
  } else if (diffDays > 0) {
    return {
      text: `${diffDays} d ago`,
      isOnline: false
    };
  } else if (diffHours > 0) {
    return {
      text: `${diffHours} h ago`,
      isOnline: false
    };
  } else {
    return {
      text: `${diffMinutes} min ago`,
      isOnline: false
    };
  }
}
</script>