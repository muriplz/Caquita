<template>
  <div class="item-info-container modal">
    <div class="item-header">
      <ItemIcon :item-id="item.id" size="256" />
      <h2 class="item-title">{{ itemName }}</h2>
    </div>

    <div class="item-details">
      <div class="info-row"><span>Rarity:</span> {{ item?.rarity || 'Unknown' }}</div>
      <div class="info-row"><span>Type:</span> {{ item?.resourceType || 'Unknown' }}</div>

      <ShapeVisualizer :item="item" />

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
import ShapeVisualizer from "@/components/ui/inventory/ShapeVisualizer.vue";
import ItemIcon from "@/components/ui/inventory/ItemIcon.vue";

const props = defineProps({
  inventoryItem: {
    type: Object,
    default: () => Store.selectedInventoryItem
  }
})

const itemImage = ref(null)
const itemName = ref('')
const item = computed(() =>
    props.inventoryItem ? Store.getItemById(props.inventoryItem.id) : null
)

const image = computed(() => {
  if (!props.inventoryItem) return ''
  return `/images/items/${props.inventoryItem.id.split(':').join('/')}.png`
})

const adjustImageSize = () => {
  if (!itemImage.value) return

  const img = itemImage.value

  if (img.naturalWidth >= img.naturalHeight) {
    // Image is wider than tall or square
    img.style.width = '256px'
    img.style.height = 'auto'
  } else {
    // Image is taller than wide
    img.style.width = 'auto'
    img.style.height = '256px'
  }
}

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
  padding: 20px;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 20px;
}

.item-title {
  font-size: 20px;
  font-weight: 600;
  margin-top: 24px;
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