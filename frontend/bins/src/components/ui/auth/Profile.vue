<template>
  <div class="profile-card">
    <div class="user-info">
      <div class="profile-avatar-container">
        <div class="avatar-circle">{{ Store.getUser().username.charAt(0).toUpperCase() }}</div>
      </div>
      <h4>@{{Store.getUser().username}}</h4>

    </div>
    <LevelBar />
    <div class="profile-details">
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
import Store from "@/js/Store.js";
import LevelBar from './LevelBar.vue';
import SyncStore from "@/js/sync/SyncStore.js";

const currencies = SyncStore.getCurrencies();

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}
</script>

<style scoped>
.profile-card {
  background-color: #fff;
  border: 3px solid black;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
}

.user-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 16px;
  gap: 8px;
}

.avatar-circle {
  width: 24px;
  height: 24px;
  border-radius: 20%;
  background-color: #373737;
  color: white;
}

</style>