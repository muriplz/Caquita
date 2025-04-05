<template>
  <div class="profile-card">
    <div v-if="user.username === Store.getUser().username" class="user-info">
      <div class="avatar-circle">{{ user.username.charAt(0).toUpperCase() }}</div>
      <div class="user-name">
        <p>@{{user.username}}</p>
        <p class="connection-status" :class="{ online: isOnline }">
          {{ connectionStatus }}
        </p>
      </div>
      <LevelBar :user="user"/>
    </div>

    <p>Trust: {{ user.trust }}</p>
    <p>Creation: {{ formatDate(user.creation) }}</p>
    <h3>Advancements</h3>

  </div>
</template>

<script setup>
import { computed } from 'vue';
import Store from "@/js/Store.js";
import LevelBar from './LevelBar.vue';

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
});

const isOnline = computed(() => {
  if (!props.user.connection) return false;
  const now = new Date();
  const connectionTime = new Date(props.user.connection);
  const diffMinutes = Math.floor((now - connectionTime) / (1000 * 60));
  return diffMinutes < 5;
});

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

const connectionStatus = computed(() => {
  if (!props.user.connection) return 'Never connected';

  const now = new Date();
  const connectionTime = new Date(props.user.connection);
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
});
</script>

<style scoped>
.profile-card {
  display: flex;
  flex-direction: column;
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
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.connection-status {
  font-size: 0.6rem;
  color: #666;
}

.connection-status.online {
  color: #42b983;
  font-weight: bold;
}

.avatar-circle {
  width: 24px;
  height: 24px;
  border-radius: 20%;
  color: white;
}
</style>