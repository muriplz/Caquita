<template>
  <div class="profile-card modal">
    <div class="user-info">

      <div class="avatar-circle">{{ Store.getUser().username.charAt(0).toUpperCase() }}</div>
      <div class="user-name">
        <p>@{{Store.getUser().username}}</p>
        <p class="creation">Since {{ formatDate(Store.getUser().creation) }}</p>
      </div>
      <LevelBar />
    </div>
  </div>
</template>

<script setup>
import Store from "@/js/Store.js";
import LevelBar from './LevelBar.vue';

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}
</script>

<style scoped>
.profile-card {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  padding: 8px;
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

.creation {
  font-size: 0.6rem;
  color: #666;
}

.avatar-circle {
  width: 24px;
  height: 24px;
  border-radius: 20%;
  background-color: #373737;
  color: white;
}

</style>