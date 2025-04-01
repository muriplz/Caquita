<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";
import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
import Store from "@/js/Store.js";
import AuthService from "@/js/auth/AuthService.js";

const route = useRoute();
const petitionId = ref(route.params.id);
const petition = ref(null);
const messages = ref(null);
const author = ref(null);

const fetchPetition = async () => {
  petition.value = await PetitionsApi.byId(petitionId.value);
};

const updateStatus = async (status) => {
  await PetitionsApi.updateStatus(petitionId.value, status);
  await fetchPetition();
};

onMounted(async () => {
  await fetchPetition();
  author.value = await AuthService.getUsername(petition.value.userId);
  messages.value = await PetitionsApi.messages(petitionId.value);
});
</script>

<template>
  <PetitionsHeader/>
  <div v-if="petition" class="petition-container">

    <div class="petition-info">
      <h1>{{ petition.title }}</h1>
      <p>At ({{ petition.lat }}, {{ petition.lon }})</p>
      <p>Status: {{ petition.status }}</p>
      <p>Created by: {{ author.username }}</p>
    </div>

    <div class="buttons">
      <button v-if="Store.getUser().trust === 'ADMINISTRATOR' && petition.status === 'PENDING'" class="reject-button" @click="updateStatus('REJECTED')">Reject</button>
      <button v-if="Store.getUser().trust === 'ADMINISTRATOR' && petition.status === 'PENDING'" class="accept-button" @click="updateStatus('ACCEPTED')">Accept</button>
    </div>

    <h2>Messages</h2>
    <pre>{{ messages }}</pre>
  </div>
  <div v-else class="loading">
    Loading petition...
  </div>
</template>

<style scoped>
.petition-container {
  padding: 20px;
  margin: 160px 0 0 0;
}

.loading {
  text-align: center;
  padding: 40px;
}

@media (max-width: 600px) {
  .petition-container {
    margin: 290px auto 0 auto;
  }
}
</style>