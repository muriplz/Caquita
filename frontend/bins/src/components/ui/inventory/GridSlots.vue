<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount, reactive } from "vue";
import Store from "@/js/Store.js";

const inventory = ref(Store.getInventory());
const forceUpdate = ref(0);

// Only update when inventory changes
watch(() => Store.getInventory(), (newInventory) => {
  inventory.value = newInventory;
});

// Memoize grid map calculation
const gridMap = computed(() => {
  forceUpdate.value;

  const grid = [];
  const height = inventory.value?.height || 8;
  const width = inventory.value?.width || 8;

  // Initialize with empty arrays
  for (let row = 0; row < height; row++) {
    grid[row] = Array(width).fill(null);
  }

  // Fill with inventory items
  if (inventory.value?.items) {
    inventory.value.items.forEach(invItem => {
      if (invItem?.cells) {
        invItem.cells.forEach(cell => {
          if (cell.row >= 0 && cell.row < height && cell.col >= 0 && cell.col < width) {
            grid[cell.row][cell.col] = invItem.id;
          }
        });
      }
    });
  }

  return grid;
});

// Optimize by only rendering cells that need to be visible
const visibleCells = computed(() => {
  forceUpdate.value;

  const cells = [];
  const grid = gridMap.value;
  const height = inventory.value?.height || 8;
  const width = inventory.value?.width || 8;

  for (let row = 0; row < height; row++) {
    for (let col = 0; col < width; col++) {
      cells.push({
        row,
        col,
        itemId: grid[row][col],
        isOccupied: grid[row][col] !== null
      });
    }
  }

  return cells;
});

function handleStoreEvents(event) {
  if (event === 'item-position-changed' || event === 'inventory-updated') {
    // Clear connection class cache when inventory changes
    Object.keys(connectionClassCache).forEach(key => {
      delete connectionClassCache[key];
    });
    forceUpdate.value++;
  }
}

onMounted(() => {
  Store.addListener(handleStoreEvents);
});

onBeforeUnmount(() => {
  Store.removeListener(handleStoreEvents);
});

// Reactive cache for connection classes to ensure proper updates
const connectionClassCache = reactive({});

function getConnectionClasses(row, col) {
  const key = `${row}-${col}`;
  const grid = gridMap.value;
  const itemId = grid[row]?.[col];

  // Return empty for non-occupied cells
  if (!itemId) return '';

  // Check cache first
  if (connectionClassCache[key]) return connectionClassCache[key];

  // Calculate connections directly from grid for reliability
  const height = inventory.value?.height || 8;
  const width = inventory.value?.width || 8;

  const connections = {
    top: row > 0 && grid[row - 1]?.[col] === itemId,
    right: col < width - 1 && grid[row]?.[col + 1] === itemId,
    bottom: row < height - 1 && grid[row + 1]?.[col] === itemId,
    left: col > 0 && grid[row]?.[col - 1] === itemId
  };

  const {top, right, bottom, left} = connections;
  const connectionCount = [top, right, bottom, left].filter(Boolean).length;

  let result = 'isolated';

  if (connectionCount === 1) {
    if (top) result = 'connected-top';
    if (right) result = 'connected-right';
    if (bottom) result = 'connected-bottom';
    if (left) result = 'connected-left';
  } else if (connectionCount === 2) {
    if (top && bottom) result = 'connected-vertical';
    else if (left && right) result = 'connected-horizontal';
    else if (top && right) result = 'connected-top-right';
    else if (top && left) result = 'connected-top-left';
    else if (bottom && right) result = 'connected-bottom-right';
    else if (bottom && left) result = 'connected-bottom-left';
  } else if (connectionCount === 3) {
    if (!top) result = 'connected-three-bottom';
    else if (!right) result = 'connected-three-left';
    else if (!bottom) result = 'connected-three-top';
    else if (!left) result = 'connected-three-right';
  } else if (connectionCount === 4) {
    result = 'connected-all';
  }

  // Store result in cache
  connectionClassCache[key] = result;
  return result;
}
</script>

<template>
  <div class="grid-slots">
    <div
        v-for="cell in visibleCells"
        :key="`${cell.row}-${cell.col}`"
        class="grid-slot"
        :class="[
          cell.isOccupied ? 'occupied' : 'empty',
          cell.isOccupied ? getConnectionClasses(cell.row, cell.col) : ''
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