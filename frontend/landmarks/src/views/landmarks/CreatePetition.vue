<script setup>
import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
import { ref, computed } from "vue";
import { LANDMARK_TYPES } from "@/views/landmarks/js/LandmarkInfo.js";
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";

const name = ref('');
const description = ref('');
const type = ref('');
const latitude = ref('');
const longitude = ref('');
const selectedFeatures = ref([]);

const availableFeatures = computed(() => {
  return type.value ? LANDMARK_TYPES[type.value] || [] : [];
});

const handleTypeChange = () => {
  selectedFeatures.value = [];
};

const submitForm = async (event) => {
  event.preventDefault();

  const landmarkInfo = {};
  selectedFeatures.value.forEach(feature => {
    landmarkInfo[feature] = true;
  });

  const success = await PetitionsApi.create(
      name.value,
      description.value,
      type.value.replace(" ", "_").toUpperCase(),
      parseFloat(latitude.value),
      parseFloat(longitude.value),
      landmarkInfo
  );

  if (success) {
    name.value = '';
    description.value = '';
    type.value = '';
    latitude.value = '';
    longitude.value = '';
    selectedFeatures.value = [];
    alert('Petition created successfully!');
  } else {
    alert('Failed to create petition');
  }
};
</script>

<template>
  <PetitionsHeader/>

  <div class="mt-50 flex justify-center">
    <form class="input-form" @submit="submitForm">
      <input type="text" placeholder="Name" v-model="name" required/>
      <input type="text" placeholder="Description" v-model="description" required/>

      <select v-model="type" @change="handleTypeChange" required>
        <option value="" disabled>Landmark type</option>
        <option v-for="(features, landmarkType) in LANDMARK_TYPES" :key="landmarkType" :value="landmarkType">
          {{ landmarkType }}
        </option>
      </select>

      <div v-if="availableFeatures.length > 0" class="mt-4">
        <h3 class="mb-2">Features:</h3>
        <div v-for="feature in availableFeatures" :key="feature" class="mb-2">
          <label class="flex items-center gap-2">
            <input type="checkbox" :value="feature" v-model="selectedFeatures"/>
            {{ feature.charAt(0).toUpperCase() + feature.slice(1) }}
          </label>
        </div>
      </div>

      <input placeholder="Latitude" type="number" step="any" v-model="latitude" required/>
      <input placeholder="Longitude" type="number" step="any" v-model="longitude" required/>

      <input type="submit" value="Submit" class="bg-blue-500 text-white cursor-pointer"/>
    </form>
  </div>
</template>

<style scoped>
.input-form input, select {
  border: 2px solid #333;
  border-radius: 4px;
  padding: 8px;
  margin-bottom: 16px;
  display: block;
  width: 100%;
}

input[type="checkbox"] {
  width: auto;
  display: inline-block;
  margin: 0;
  vertical-align: middle;
}
</style>