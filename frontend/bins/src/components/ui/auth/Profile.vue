<template>
  <div class="profile-card">
    <h2>User Profile</h2>
    <div class="profile-avatar-container">
      <div class="avatar-circle">{{ Store.getUser().username.charAt(0).toUpperCase() }}</div>
    </div>
    <LevelBar />
    <div class="profile-details">
      <div class="profile-item">
        <div class="label">Username</div>
        <div class="value">{{ Store.getUser().username }}</div>
      </div>
      <div class="profile-item" v-if="Store.getUser().creation">
        <div class="label">Joined</div>
        <div class="value">{{ formatDate(Store.getUser().creation) }}</div>
      </div>
      <div class="profile-item" v-if="Store.getUser().trust">
        <div class="label">Trust Level</div>
        <div class="value">{{ Store.getUser().trust }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted} from 'vue';
import Store from "@/js/auth/store.js";
import LevelBar from './LevelBar.vue';
import Levels from '@/js/auth/levels.js';

onMounted(async () => {
  if (Store.getUser() && !Store.getLevel()) {
    try {
      console.log("User ID in Profile component:", Store.getUser().id);
      await Levels.setup();
    } catch (error) {
      console.error("Failed to load user level:", error);
    }
  }
});

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}
</script>

<style scoped>
.profile-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  width: 360px;
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-card h2 {
  color: #333;
  margin-bottom: 25px;
  font-size: 24px;
  font-weight: 600;
}

.profile-avatar-container {
  position: relative;
  margin-bottom: 30px;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #4a6da7;
  color: white;
  font-size: 36px;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  z-index: 2;
}


.profile-details {
  width: 100%;
  margin-top: 20px;
}

.profile-item {
  margin-bottom: 15px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.value {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}
</style>