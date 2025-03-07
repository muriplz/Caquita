<template>
  <div class="vitrine-container" :class="{ 'is-loading': loading }">
    <div class="vitrine-header">
      <h2>Vitrine</h2>
      <div class="vitrine-actions">
        <button
            class="refresh-button"
            @click="refreshVitrine"
            :disabled="loading"
        >
          Refresh
        </button>
      </div>
    </div>

    <div v-if="items.length > 0" class="vitrine-grid">
      <div
          v-for="item in items"
          :key="item.id"
          class="vitrine-item"
          :class="`rarity-${item.rarity || 'common'}`"
      >
        <div class="item-image">
          <!-- Item image or placeholder -->
          <div class="item-placeholder">{{ item.type.charAt(0) }}</div>
        </div>
        <div class="item-details">
          <div class="item-name">{{ item.id }}</div>
          <div class="item-rarity">{{ item.rarity || 'Common' }}</div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-vitrine-message">
      No items to display
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
    </div>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue';
import {getAllItems} from '@/js/items/items.js';

const items = ref([]);
const loading = ref(false);
const error = ref(null);

const refreshVitrine = async () => {
  try {
    loading.value = true;
    const fetchedItems = await getAllItems();
    items.value = fetchedItems;
  } catch (err) {
    error.value = err.message;
    console.error('Failed to fetch items:', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  refreshVitrine();
});

onBeforeUnmount(() => {
  // Cleanup if needed
});
</script>

<style scoped>
.vitrine-container {
  width: 90%;
  max-width: 800px;
  background-color: black;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  pointer-events: auto;
  color: white;
  position: relative;
}

.vitrine-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vitrine-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

.refresh-button {
  padding: 8px 16px;
  background-color: #444;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.refresh-button:hover:not(:disabled) {
  background-color: #555;
}

.refresh-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.vitrine-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
  padding: 16px;
  background-color: #333;
  border-radius: 8px;
}

.vitrine-item {
  background-color: #444;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.vitrine-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.item-image {
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #222;
}

.item-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #555;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
}

.item-details {
  padding: 10px;
}

.item-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.item-rarity {
  font-size: 0.8em;
  color: #aaa;
}

.rarity-common {
  border: 1px solid #aaa;
}

.rarity-uncommon {
  border: 1px solid #4CAF50;
}

.rarity-rare {
  border: 1px solid #2196F3;
}

.rarity-epic {
  border: 1px solid #9C27B0;
}

.rarity-legendary {
  border: 1px solid #FFC107;
}

.empty-vitrine-message {
  padding: 40px;
  text-align: center;
  background-color: #333;
  border-radius: 8px;
  color: #ccc;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 12px;
  z-index: 10;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-left-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>