<script setup>
import {ref, reactive, onMounted, computed, watch} from 'vue';
import TopDownMapSection from "@/views/landmarks/TopDownMapSection.vue";
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";
import {startGPSTracking} from "@/components/player/GPSTracker.js";
import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
import {LANDMARK_FEATURES} from "@/views/landmarks/js/LandmarkInfo.js";
import LandmarkTypes from "@/js/landmarks/LandmarkTypes.js";
import ImageUploader from "./ImageUploader.vue";

const mapRef = ref(null);
const formData = reactive({
  name: '',
  description: '',
  type: LandmarkTypes.LANDMARK_TYPES.TRASH_CAN,
  position: null,
  features: {}
});

// Store the random name placeholder in a ref so it doesn't change on every render
const randomNamePlaceholder = ref('');

// Image handling
const selectedImage = ref(null);
const uploadStatus = ref({status: 'idle', message: ''});
const petitionId = ref(null);

// Create dropdown options from landmark types
const typeOptions = Object.entries(LandmarkTypes.LANDMARK_TYPES).map(([key, value]) => ({
  label: key.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase()),
  value
}));

const isLoading = ref(false);
const error = ref(null);
const success = ref(false);

// Form validation
const isFormValid = computed(() => {
  return (
      formData.name.trim() !== '' &&
      formData.description.trim() !== '' &&
      formData.position !== null &&
      selectedImage.value !== null
  );
});

onMounted(() => {
  startGPSTracking();
  initializeFeatures();
  // Set the random name once on mount
  randomNamePlaceholder.value = getRandomName();
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
      formData.features[feature] = false;
    });
  }
};

const getFeatureEnabled = (feature) => {
  return formData.features[feature];
};

const toggleFeature = (feature) => {
  formData.features[feature] = !formData.features[feature];
};

const onPositionSelected = (position) => {
  formData.position = position;
};

const handleImageUpdate = (image) => {
  selectedImage.value = image;
  error.value = null;
};

const handleUploadStatus = (status) => {
  uploadStatus.value = status;
  if (status.status === 'error') {
    error.value = status.message;
  } else {
    error.value = null;
  }
};

const uploadImage = async () => {
  if (!selectedImage.value || !petitionId.value) return false;

  try {
    uploadStatus.value = {status: 'uploading', message: 'Uploading image...'};
    await PetitionsApi.uploadImage(petitionId.value, selectedImage.value);
    uploadStatus.value = {status: 'success', message: 'Upload successful'};
    return true;
  } catch (err) {
    uploadStatus.value = {status: 'error', message: 'Failed to upload image'};
    error.value = 'Failed to upload image: ' + err.message;
    return false;
  }
};

const submitForm = async (e) => {
  e.preventDefault();

  if (!isFormValid.value) {
    error.value = selectedImage.value ? "Please fill in all required fields" : "Please add a photo - it's required";
    return;
  }

  try {
    isLoading.value = true;
    error.value = null;

    const landmarkInfo = {
      name: formData.name,
      ...Object.entries(formData.features).reduce((acc, [feature, enabled]) => {
        acc[feature] = enabled.toString();
        return acc;
      }, {})
    };

    const createSuccess = await PetitionsApi.create(
        formData.description,
        formData.type,
        formData.position.lat,
        formData.position.lng,
        landmarkInfo
    );

    if (createSuccess) {
      try {
        const petitions = await PetitionsApi.get(0, 'DESC', 'PENDING');
        if (petitions && petitions.petitions && petitions.petitions.length > 0) {
          petitionId.value = petitions.petitions[0].id;

          if (selectedImage.value) {
            const imageUploaded = await uploadImage();
            success.value = true;
            if (!imageUploaded) {
              error.value = "Petition created but the image failed to upload";
            }
          }
        } else {
          success.value = true;
        }
      } catch (fetchErr) {
        console.error('Failed to fetch petition ID:', fetchErr);
        success.value = true;
        error.value = "Petition created but couldn't retrieve petition ID for image upload";
      }
    }

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
      <p>Your petition has been successfully created!</p>
      <div v-if="uploadStatus.status === 'success'" class="image-success">
        <p>Your image was uploaded successfully.</p>
      </div>
      <div v-else-if="uploadStatus.status === 'error'" class="error-message">
        <p>Your petition was created, but there was an error uploading the image.</p>
      </div>
    </div>

    <form v-else @submit="submitForm">
      <div class="form-group">
        <label for="petition-name">Give it an original name!</label>
        <input
            id="petition-name"
            type="text"
            v-model="formData.name"
            :placeholder="randomNamePlaceholder"
            required
            autocomplete="off"
        />
      </div>

      <div class="form-group">
        <label for="petition-description">Description</label>
        <textarea
            id="petition-description"
            v-model="formData.description"
            placeholder="Tell us about this landmark..."
            required
            rows="4"
        ></textarea>
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
              @click="toggleFeature(feature)"
          >
            <div class="feature-checkbox" :class="{'feature-enabled': getFeatureEnabled(feature)}">
              <span v-if="getFeatureEnabled(feature)">✓</span>
            </div>
            <span class="feature-label">{{ feature }}</span>
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

      <div class="form-group">
        <label>Add a photo <span class="required-marker">*</span></label>
        <ImageUploader
            v-model:image="selectedImage"
            @upload-status="handleUploadStatus"
        />
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
  color: #aeaeae;
}

.required-marker {
  color: #f44336;
}

input, select, textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
  font: inherit;
}

textarea {
  resize: vertical;
  min-height: 100px;
}

.location-help {
  margin-top: 5px;
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
  margin-bottom: 24px;
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

.image-success {
  margin-top: 10px;
  font-size: 16px;
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

.feature-enabled {
  background: #4CAF50;
  color: white;
  border-color: #4CAF50;
}

.feature-label {
  text-transform: capitalize;
}

@media (max-width: 600px) {
  .container {
    margin: 290px auto 0 auto;
  }

  .features-container {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>