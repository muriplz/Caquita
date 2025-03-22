<script setup>
import { onBeforeMount, ref } from "vue";
import Store from "@/js/Store.js";
import GridSlots from "@/components/ui/inventory/GridSlots.vue";
import InventoryUtils from "@/components/ui/inventory/js/InventoryUtils.js";

const inventory = ref({})
const items = ref([])
const allItems = ref([])
const occupiedCells = ref({})

onBeforeMount(() => {
  inventory.value = Store.getInventory()
  allItems.value = Store.getItems()
  processInventoryItems()
})

const processInventoryItems = () => {
  items.value = []
  occupiedCells.value = {}

  if (inventory.value.items && inventory.value.items.length > 0) {
    inventory.value.items.forEach(inventoryItem => {
      const item = allItems.value.find(i => i.id === inventoryItem.id)
      if (!item) return

      const firstCellIndex = inventoryItem.cells[0]
      const baseRow = Math.floor(firstCellIndex / inventory.value.width)
      const baseCol = firstCellIndex % inventory.value.width

      // Generate actual occupied cells based on the item's shape
      const cellsToOccupy = []

      if (item.shape && item.shape.length > 0) {
        const dimensions = InventoryUtils.getActualDimensions(item.shape)

        for (let r = 0; r < item.shape.length; r++) {
          for (let c = 0; c < item.shape[r].length; c++) {
            if (item.shape[r][c] === 1) {
              const cellRow = baseRow + r - dimensions.minRow
              const cellCol = baseCol + c - dimensions.minCol
              const cellIndex = cellRow * inventory.value.width + cellCol
              cellsToOccupy.push(cellIndex)
            }
          }
        }
      } else {
        cellsToOccupy.push(firstCellIndex)
      }

      // Mark cells as occupied
      cellsToOccupy.forEach(cellIndex => {
        occupiedCells.value[cellIndex] = {
          id: item.id,
          inventoryItemId: firstCellIndex
        }
      })

      items.value.push({
        id: item.id,
        inventoryItemId: firstCellIndex,
        row: baseRow,
        col: baseCol,
        shape: item.shape,
        cells: cellsToOccupy,
        rarity: item.rarity
      })
    })
  }
}

const handleItemMoved = (item, newSlot) => {
  // The GridSlots component already updates the local state
  // We just need to ensure the Store is updated
  Store.updateItemPosition(item, newSlot)
}
</script>

<template>
  <div class="inventory-grid">
    <GridSlots
        :occupied-cells="occupiedCells"
        :items="items"
        @item-moved="handleItemMoved"
        v-model:occupied-cells="occupiedCells"
        v-model:items="items"
    />
  </div>
</template>

<style scoped>
.inventory-grid {
  display: grid;
  grid-template-rows: repeat(v-bind('inventory.height'), 64px);
  grid-template-columns: repeat(v-bind('inventory.width'), 64px);
  gap: 8px;
  background-color: #252525;
  padding: 12px;
  border-radius: 6px;
  position: relative;
}
</style>