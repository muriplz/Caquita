<template>
  <div class="profile-card">
    <div v-if="user?.username === Store.getUser()?.username">
      <LevelBar :user="user"/>
    </div>

    <p class="logout" @click="AuthService.logout()">Logout</p>
  </div>
</template>

<script setup>
import Store from "@/js/Store.js";
import LevelBar from './LevelBar.vue';
import AuthService from "@/js/auth/AuthService.js";

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
});
</script>

<style scoped>
.logout {
  color: #bf2323;
  position: absolute;
  bottom: 0;
  width: 100%;
  text-align: end;
  cursor: pointer;
}
</style>