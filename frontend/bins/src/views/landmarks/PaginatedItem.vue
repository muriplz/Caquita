<script setup>
import {createRouter as route} from "vue-router";
import router from "@/router/index.js";

defineProps({
  petition: Object
})

// Transform key from camelCase to Title Case
function formatKey(key) {
  return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
}
</script>

<template>
  <div class="container" @click="router.push('/landmarks/' + petition.id)">
    <h3>{{ petition.type }}</h3>
    <p>Coordinates: {{ petition.lat }}, {{ petition.lon }}</p>

    <div class="petition-details">
      <div v-for="(value, key) in petition.landmarkInfo" :key="key" class="detail-item">
        <strong>{{ formatKey(key) }}:</strong> {{ value }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 15px;
  background-color: #ccc;
  border-radius: 5px;
  margin-bottom: 1rem;
  cursor: pointer;
}
</style>