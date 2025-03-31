<script setup>
import { ref, reactive, onMounted } from 'vue';
import TopDownMapSection from "@/views/landmarks/TopDownMapSection.vue";
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";
import { startGPSTracking, getGPSStatus } from "@/components/player/GPSTracker.js";
import LandmarkTypes from "@/js/landmarks/LandmarkTypes.js";
import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";

const mapRef = ref(null);
const formData = reactive({
  name: '',
  type: LandmarkTypes.LandmarkTypes.TRASH_CAN,
  position: null
});

const typeOptions = Object.entries(LandmarkTypes.LandmarkTypes).map(([key, value]) => ({
  label: key.replace('_', ' ').toLowerCase(),
  value
}));

const isLoading = ref(false);
const error = ref(false);
const success = ref(false);

onMounted(() => {
  startGPSTracking();
});

const onPositionSelected = (position) => {
  formData.position = position;
};

const submitForm = async (e) => {
  e.preventDefault();

  if (!formData.name || !formData.position) {
    error.value = "Please fill in all required fields";
    return;
  }

  try {
    isLoading.value = true;
    error.value = null;

    const landmarkInfo = {
      name: formData.name
    };

    await PetitionsApi.create(
        formData.type,
        formData.position.lat,
        formData.position.lng,
        landmarkInfo
    );

    success.value = true;

  } catch (err) {
    error.value = "Failed to create petition: " + err.message;
  } finally {
    isLoading.value = false;
  }
};

const toggleFullscreen = () => {
  if (mapRef.value) {
    mapRef.value.setFullscreen(true);
  }
};

const getRandomName = () => {
  const names = ['Lullaby', 'Pablo']
  return names[Math.floor(Math.random() * names.length)];
};
</script>

<template>
  <PetitionsHeader/>

  <div class="container">


    <div v-if="success" class="success-message">
      Your petition has been successfully created!
    </div>

    <form v-else @submit="submitForm">
      <div class="form-group">
        <label for="petition-name">Give it an original name!</label>
        <input
            id="petition-name"
            type="text"
            v-model="formData.name"
            :placeholder="getRandomName()"
            required
            autocomplete="off"
        />
      </div>

      <div class="form-group">
        <label for="petition-type">Type of landmark</label>
        <select id="petition-type" v-model="formData.type">
          <option v-for="option in typeOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label>Location</label>
        <p class="location-help">Select a location on the map</p>

        <div class="map-wrapper">
          <TopDownMapSection ref="mapRef" @position-selected="onPositionSelected"/>
          <button type="button" class="expand-map-btn" @click="toggleFullscreen">Expand map</button>
        </div>

        <div v-if="formData.position" class="selected-location">
          Selected: {{ formData.position.lat }}, {{ formData.position.lng }}
        </div>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>

      <div class="form-actions">
        <button type="submit" class="submit-btn" :disabled="isLoading">
          {{ isLoading ? 'Creating...' : 'Create Petition' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.container {
  max-width: 800px;
  margin: 160px 0 0 0;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-weight: bold;
  margin-bottom: 8px;
}

input, select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
}

.location-help {
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.map-wrapper {
  position: relative;
  margin-bottom: 10px;
}

.expand-map-btn {
  position: absolute;
  bottom: 10px;
  right: 10px;
  z-index: 5;
  background: #fff;
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
}

.selected-location {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
}

.form-actions {
  margin-top: 30px;
}

.submit-btn {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  font: inherit;
}

.submit-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.success-message {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 20px;
  border-radius: 4px;
  text-align: center;
  font-size: 18px;
}

@media (max-width: 600px) {
  .container {
    margin: 290px auto 0 auto;
  }
}
</style>