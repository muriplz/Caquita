<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import TopDownMapSection from "@/views/landmarks/TopDownMapSection.vue";
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";
import { startGPSTracking } from "@/components/player/GPSTracker.js";
import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
import { LANDMARK_FEATURES, LEVEL } from "@/views/landmarks/js/LandmarkInfo.js";
import LandmarkTypes from "@/js/landmarks/LandmarkTypes.js";

const mapRef = ref(null);
const formData = reactive({
  name: '',
  type: LandmarkTypes.LANDMARK_TYPES.TRASH_CAN,
  position: null,
  features: {}
});

// Create dropdown options from landmark types
const typeOptions = Object.entries(LandmarkTypes.LANDMARK_TYPES).map(([key, value]) => ({
  label: key.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase()),
  value
}));

const isLoading = ref(false);
const error = ref(false);
const success = ref(false);

onMounted(() => {
  startGPSTracking();
  initializeFeatures();
});

// Computed property to get available features based on the selected type
const availableFeatures = computed(() => {
  return LANDMARK_FEATURES[formData.type] || [];
});

// Watch for type changes to reinitialize features
watch(() => formData.type, () => {
  initializeFeatures();
});

// Initialize feature values based on selected landmark type
const initializeFeatures = () => {
  formData.features = {};
  const features = LANDMARK_FEATURES[formData.type];
  if (features) {
    features.forEach(feature => {
      formData.features[feature] = null;
    });
  }
};

const getFeatureLevel = (feature) => {
  return formData.features[feature];
};

const getNextLevel = (currentLevel) => {
  if (currentLevel === null) return LEVEL[0];
  const currentIndex = LEVEL.indexOf(currentLevel);
  const nextIndex = (currentIndex + 1) % LEVEL.length;
  return nextIndex === 0 ? null : LEVEL[nextIndex];
};

// You might also want to add styling for NONE level
const getLevelClass = (level) => {
  if (level === 'HIGH') return 'level-high';
  if (level === 'MEDIUM') return 'level-medium';
  if (level === 'LOW') return 'level-low';
  if (level === 'NONE') return 'level-none';
  return '';
};

const cycleFeatureLevel = (feature) => {
  formData.features[feature] = getNextLevel(formData.features[feature]);
};

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

    // Create a flat object with name and features
    const landmarkInfo = {
      name: formData.name,
      // Include all features, with NONE for null values
      ...Object.entries(formData.features).reduce((acc, [feature, level]) => {
        acc[feature] = level === null ? "NONE" : level;
        return acc;
      }, {})
    };

    success.value = await PetitionsApi.create(
        formData.type,
        formData.position.lat,
        formData.position.lng,
        landmarkInfo
    );

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
  const names = ['Lullaby', 'Dusk', 'Merendola']
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

      <div v-if="availableFeatures.length > 0" class="form-group">
        <label>Features</label>
        <div class="features-container">
          <div
              v-for="feature in availableFeatures"
              :key="feature"
              class="feature-item"
              @click="cycleFeatureLevel(feature)"
          >
            <div class="feature-checkbox" :class="getLevelClass(getFeatureLevel(feature))">
              <span v-if="getFeatureLevel(feature)">{{ getFeatureLevel(feature).charAt(0) }}</span>
            </div>
            <span class="feature-label">{{ feature }}</span>
          </div>
        </div>
        <div class="legend">
          <div class="legend-item">
            <div class="feature-checkbox level-high"><span>H</span></div>
            <span>High</span>
          </div>
          <div class="legend-item">
            <div class="feature-checkbox level-medium"><span>M</span></div>
            <span>Medium</span>
          </div>
          <div class="legend-item">
            <div class="feature-checkbox level-low"><span>L</span></div>
            <span>Low</span>
          </div>
          <div class="legend-item">
            <div class="feature-checkbox"></div>
            <span>None</span>
          </div>
        </div>
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
  font: inherit;
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
  background: rgba(213, 179, 63, 0.99);
  border: 3px solid #333;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
  font: inherit;
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
.features-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}
.feature-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  background: #f5f5f5;
  cursor: pointer;
}
.feature-checkbox {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #ccc;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}
.level-high {
  background: #4CAF50;
  color: white;
  border-color: #4CAF50;
}
.level-medium {
  background: #FFC107;
  color: black;
  border-color: #FFC107;
}
.level-low {
  background: #F44336;
  color: white;
  border-color: #F44336;
}
.level-none {
  background: #e0e0e0;
  color: #757575;
  border-color: #e0e0e0;
}
.feature-label {
  text-transform: capitalize;
}
.legend {
  display: flex;
  margin-top: 10px;
  gap: 15px;
  padding: 8px;
  background: #f9f9f9;
  border-radius: 4px;
}
.legend-item {
  display: flex;
  align-items: center;
}
.legend-item .feature-checkbox {
  width: 18px;
  height: 18px;
  margin-right: 5px;
}
@media (max-width: 600px) {
  .container {
    margin: 290px auto 20px auto;
  }
  .features-container {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
  .legend {
    flex-wrap: wrap;
  }
}
</style>