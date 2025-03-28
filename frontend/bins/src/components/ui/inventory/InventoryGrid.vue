<template>
  <div class="inventory-container">
    <div ref="gridRef" class="inventory-grid" :style="gridStyle">
      <!-- Visual layer for connected slots -->
      <GridSlots />

      <!-- Draggable items -->
      <InventoryItem
          v-for="item in inventoryItems"
          :key="item.id"
          :inventory-item="item"
          @itemClicked="showItemInfo"
      />
    </div>
  </div>
</template>

<script setup>
import {computed, ref, watch} from "vue"
import Store from "@/js/Store.js"
import InventoryItem from "./InventoryItem.vue"
import GridSlots from "./GridSlots.vue"
import uiRouter from "../UIRouter.js"

const inventory = ref(Store.getInventory())
const inventoryItems = computed(() => inventory.value?.items || [])
const gridRef = ref(null)

// Watch for store updates
watch(() => Store.getInventory(), (newInventory) => {
  inventory.value = newInventory
}, { deep: true })

// Grid style based on inventory dimensions
const gridStyle = computed(() => {
  // Use default values if inventory isn't loaded yet
  const width = inventory.value?.width || 8
  const height = inventory.value?.height || 8

  return {
    width: `${width * 64 + (width - 1) * 4}px`,
    height: `${height * 64 + (height - 1) * 4}px`,
    position: 'relative'
  }
})

// Safe grid template values for binding
const gridTemplateColumns = computed(() => {
  const width = inventory.value?.width || 8
  return `repeat(${width}, 64px)`
})

const gridTemplateRows = computed(() => {
  const height = inventory.value?.height || 8
  return `repeat(${height}, 64px)`
})

// Method to show item info
function showItemInfo(item) {
  if (item) {
    Store.selectedInventoryItem = item
    uiRouter.navigate('ITEM_INFO')
  }
}
</script>

<style scoped>
.inventory-container {
  padding: 8px;
  background-color: #fff;
  border: 4px solid #333;
  width: fit-content;
}

.inventory-grid {
  display: grid;
  grid-template-columns: v-bind(gridTemplateColumns);
  grid-template-rows: v-bind(gridTemplateRows);
  gap: 4px;
  position: relative;
}
</style>