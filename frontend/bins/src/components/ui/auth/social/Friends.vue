<script setup>
import {computed, onBeforeMount, ref} from "vue";
import FriendshipApi from "./js/FriendshipApi.js";

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

  console.log(friend.level.value.level);
  const now = new Date();
  const connectionTime = new Date(friend.connection);
  const diffMs = now - connectionTime;

  // Less than 30 minutes
  if (diffMs < 30 * 60 * 1000) {
    return 'ONLINE';
  }

  const diffMinutes = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);
  const diffMonths = Math.floor(diffDays / 30);
  const diffYears = Math.floor(diffMonths / 12);

  if (diffYears > 0) {
    return `${diffYears} y ago`;
  } else if (diffMonths > 0) {
    return `${diffMonths} m ago`;
  } else if (diffDays > 0) {
    return `${diffDays} d ago`;
  } else if (diffHours > 0) {
    return `${diffHours} h ago`;
  } else {
    return `${diffMinutes} min ago`;
  }
}
</script>

<template>
  <div v-if="friends && friends.length > 0" class="friends-list">
    <div v-for="friend in friends" :key="friend.id" class="friend-item">
      <div class="friend-info">
        <img class="avatar" :src="`/images/ui/avatar/${avatars[friend.id]}.png`" />
        <div class="friend-details">
          <div class="name-level">
            <h3 class="username">{{ friend.username }}</h3>
            <p class="level">( {{ JSON.parse(friend.level.value).level }} )</p>
          </div>
          <p class="connection-status">{{ connectionStatus(friend) }}</p>
        </div>
      </div>
      <img class="remove-friend-btn" @click="removeFriend(friend.id)" src="/images/ui/remove_friend.png" alt="Remove Friend" />
    </div>
  </div>
  <div v-else class="empty-state">No friends yet</div>
</template>

<style scoped>
.friends-list {
  width: 100%;
}

.friend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.friend-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
}

.name-level {
  display: flex;
  align-items: center;
  gap: 4px;
}

.username {
  margin: 0;
  padding-right: 2px;
  font-size: 1.1rem;
}

.level {
  margin: 0;
  font-size: 0.9rem;
}

.connection-status {
  margin: 0;
  font-size: 0.85rem;
  color: #666;
}

.avatar {
  width: 72px;
  image-rendering: pixelated;
}

.remove-friend-btn {
  width: 24px;
  height: 24px;
  cursor: pointer;
  image-rendering: pixelated;
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: #666;
}
</style>