<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from "vue";
import Store from "@/js/Store.js";

const inventory = ref(Store.getInventory());
const forceUpdate = ref(0); // Used to force reactivity

// Make component reactive to inventory changes
watch(() => Store.getInventory(), (newInventory) => {
  inventory.value = newInventory;
}, { deep: true });

// Create a 2D grid representation to track occupied cells
const gridMap = computed(() => {
  // Force computed to re-evaluate when forceUpdate changes
  forceUpdate.value;

  const grid = [];
  const height = inventory.value?.height || 8;
  const width = inventory.value?.width || 8;

  // Initialize empty grid
  for (let row = 0; row < height; row++) {
    grid[row] = [];
    for (let col = 0; col < width; col++) {
      grid[row][col] = null;
    }
  }

  // Fill with inventory items if available
  if (inventory.value?.items) {
    inventory.value.items.forEach(invItem => {
      if (invItem?.cells) {
        invItem.cells.forEach(cell => {
          if (grid[cell.row] && grid[cell.row][cell.col] !== undefined) {
            grid[cell.row][cell.col] = invItem.id;
          }
        });
      }
    });
  }

  return grid;
});

// Listen for store events
function handleStoreEvents(event) {
  if (event === 'item-position-changed' || event === 'inventory-updated') {
    // Increment forceUpdate to trigger reactivity
    forceUpdate.value++;
  }
}

onMounted(() => {
  // Register listener
  Store.addListener(handleStoreEvents);
});

onBeforeUnmount(() => {
  // Clean up listener
  Store.removeListener(handleStoreEvents);
});

// Get all cells (occupied and empty)
const allCells = computed(() => {
  // Also depend on forceUpdate to ensure reactivity
  forceUpdate.value;

  const cells = [];
  const height = inventory.value?.height || 8;
  const width = inventory.value?.width || 8;

  for (let row = 0; row < height; row++) {
    for (let col = 0; col < width; col++) {
      const itemId = gridMap.value[row]?.[col];
      cells.push({
        row,
        col,
        itemId,
        isOccupied: itemId !== null
      });
    }
  }

  return cells;
});

// Rest of the code remains the same
function getConnections(row, col, itemId) {
  const grid = gridMap.value;
  const height = inventory.value?.height || 8;
  const width = inventory.value?.width || 8;

  if (!itemId) {
    return {
      top: false,
      right: false,
      bottom: false,
      left: false
    };
  }

  return {
    top: row > 0 && grid[row - 1]?.[col] === itemId,
    right: col < width - 1 && grid[row]?.[col + 1] === itemId,
    bottom: row < height - 1 && grid[row + 1]?.[col] === itemId,
    left: col > 0 && grid[row]?.[col - 1] === itemId
  };
}

function getConnectionClasses(connections) {
  const {top, right, bottom, left} = connections;
  const connectionCount = [top, right, bottom, left].filter(Boolean).length;

  if (connectionCount === 0) return 'isolated';

  if (connectionCount === 1) {
    if (top) return 'connected-top';
    if (right) return 'connected-right';
    if (bottom) return 'connected-bottom';
    if (left) return 'connected-left';
  }

  if (connectionCount === 2) {
    if (top && bottom) return 'connected-vertical';
    if (left && right) return 'connected-horizontal';
    if (top && right) return 'connected-top-right';
    if (top && left) return 'connected-top-left';
    if (bottom && right) return 'connected-bottom-right';
    if (bottom && left) return 'connected-bottom-left';
  }

  if (connectionCount === 3) {
    if (!top) return 'connected-three-bottom';
    if (!right) return 'connected-three-left';
    if (!bottom) return 'connected-three-top';
    if (!left) return 'connected-three-right';
  }

  return 'connected-all';
}
</script>

<template>
  <div class="grid-slots">
    <div
        v-for="cell in allCells"
        :key="`${cell.row}-${cell.col}`"
        class="grid-slot"
        :class="[
          cell.isOccupied ? 'occupied' : 'empty',
          cell.isOccupied ? getConnectionClasses(getConnections(cell.row, cell.col, cell.itemId)) : ''
        ]"
        :style="{
          gridRowStart: cell.row + 1,
          gridColumnStart: cell.col + 1
        }"
    ></div>
  </div>
</template>

<style scoped>
.grid-slots {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: grid;
  gap: 4px;
  pointer-events: none;
}

.grid-slot {
  width: 64px;
  height: 64px;
  border-radius: 24px;
  box-sizing: border-box;
}

.empty {
  background-color: rgba(82, 82, 82, 0.2);
  border: 2px solid rgba(98, 98, 98, 0.2);
}

.occupied {
  background-color: rgba(56, 56, 56, 0.5);
  border: 2px solid rgba(114, 114, 114, 0.5);
}

.isolated {
  border-radius: 24px;
}

.connected-top {
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

.connected-right {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.connected-bottom {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

.connected-left {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.connected-vertical {
  border-radius: 0;
}

.connected-horizontal {
  border-radius: 0;
}

.connected-top-right {
  border-radius: 0;
  border-bottom-left-radius: 24px;
}

.connected-top-left {
  border-radius: 0;
  border-bottom-right-radius: 24px;
}

.connected-bottom-right {
  border-radius: 0;
  border-top-left-radius: 24px;
}

.connected-bottom-left {
  border-radius: 0;
  border-top-right-radius: 24px;
}

.connected-three-top {
  border-radius: 0;
  border-bottom-left-radius: 24px;
  border-bottom-right-radius: 24px;
}

.connected-three-right {
  border-radius: 0;
  border-top-left-radius: 24px;
  border-bottom-left-radius: 24px;
}

.connected-three-bottom {
  border-radius: 0;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
}

.connected-three-left {
  border-radius: 0;
  border-top-right-radius: 24px;
  border-bottom-right-radius: 24px;
}

.connected-all {
  border-radius: 0;
}
</style>