<template>
  <div class="item-info-container">
    <div class="item-header">
      <img :src="image" alt="Item image" class="item-image" />
      <h2 class="item-title">{{ itemName }}</h2>
    </div>

    <div class="item-details">
      <div class="info-row"><span>Rarity:</span> {{ item?.rarity || 'Unknown' }}</div>
      <div class="info-row"><span>Type:</span> {{ item?.resourceType || 'Unknown' }}</div>
      <div class="info-row"><span>Shape:</span> {{ item?.shape ? item.shape.join('×') : 'Unknown' }}</div>

      <div v-if="item?.disposalOutcomes?.length" class="outcomes-section">
        <div class="section-title">Disposal Outcomes:</div>
        <div v-for="(outcome, idx) in item.disposalOutcomes" :key="idx" class="outcome-item">
          {{ outcome }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Store from "@/js/Store.js"

const props = defineProps({
  inventoryItem: {
    type: Object,
    default: () => Store.selectedInventoryItem
  }
})

const itemName = ref('')
const item = computed(() =>
    props.inventoryItem ? Store.getItemById(props.inventoryItem.id) : null
)

const image = computed(() => {
  if (!props.inventoryItem) return ''
  return `/images/items/${props.inventoryItem.id.split(':').join('/')}.png`
})

onMounted(async () => {
  if (item.value) {
    try {
      itemName.value = await item.value.getName()
    } catch (e) {
      itemName.value = props.inventoryItem?.id || 'Unknown Item'
    }
  } else {
    itemName.value = props.inventoryItem?.id || 'Unknown Item'
  }
})
</script>

<style scoped>
.item-info-container {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.item-image {
  width: 64px;
  height: 64px;
  margin-right: 16px;
  image-rendering: pixelated;
}

.item-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.item-details {
  margin-bottom: 20px;
}

.info-row {
  margin-bottom: 10px;
}

.info-row span {
  font-weight: 600;
  margin-right: 6px;
}

.outcomes-section {
  margin-top: 12px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.outcome-item {
  background-color: #f5f5f5;
  padding: 8px;
  margin-bottom: 6px;
  border-radius: 4px;
}

.item-actions {
  display: flex;
  justify-content: flex-end;
}

.close-button {
  background-color: #333;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.close-button:hover {
  background-color: #555;
}
</style>