<template>
  <div class="profile-card">
    <div v-if="user?.username === Store.getUser()?.username">
      <LevelBar :user="user"/>
    </div>

    <h3>Advancements</h3>
    <p class="logout" @click="AuthService.logout()">Logout</p>
  </div>
</template>

<script setup>
import Store from "@/js/Store.js";
import LevelBar from './LevelBar.vue';
import AuthService from "../../../js/auth/AuthService.js";

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
});
</script>

<style scoped>
.profile-card {
}

.logout {
  color: #bf2323;
  position: absolute;
  bottom: -12px;
  width: 100%;
  text-align: end;
  cursor: pointer;
}
</style>