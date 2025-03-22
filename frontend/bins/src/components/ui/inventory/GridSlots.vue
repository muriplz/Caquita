<script setup>
import { ref, computed, onBeforeUnmount, inject } from "vue";
import Store from "@/js/Store.js";
import GridSlot from "@/components/ui/inventory/GridSlot.vue";
import InventoryApi from "@/components/ui/inventory/js/InventoryApi.js";
import InventoryUtils from "@/components/ui/inventory/js/InventoryUtils.js";
import InventoryManager from "@/components/ui/inventory/js/InventoryManager.js";

const props = defineProps({
  occupiedCells: {
    type: Object,
    required: true
  },
  items: {
    type: Array,
    required: true
  },
  onItemMoved: {
    type: Function,
    default: () => {}
  }
});

const emit = defineEmits(['item-moved', 'update:occupiedCells', 'update:items']);

const inventoryManager = new InventoryManager(Store);
const inventory = ref(Store.getInventory());
const allItems = ref(Store.getItems());
const dragging = ref(false);
const draggedItemId = ref(null);
const draggedItemData = ref(null);
const dragStartPos = ref({ x: 0, y: 0 });
const dragCurrentPos = ref({ x: 0, y: 0 });
const dragOffset = ref({ x: 0, y: 0 });
const currentMouseSlot = ref(null); // Slot under cursor
const isValidDrop = ref(false);

const localOccupiedCells = ref({...props.occupiedCells});
const localItems = ref([...props.items]);

const itemGroups = computed(() => {
  const groups = {};

  localItems.value.forEach(item => {
    const itemDef = getItemDefinition(item.id);
    if (!itemDef) return;

    const shape = itemDef.shape;
    const dimensions = InventoryUtils.getActualDimensions(shape);

    groups[item.inventoryItemId] = {
      id: item.id,
      cells: item.cells,
      row: item.row,
      col: item.col,
      width: dimensions.width,
      height: dimensions.height,
      imagePath: `/images/items/${item.id.split(':')[0]}/${item.id.split(':')[1]}.png`,
    };
  });

  return groups;
});

const slotData = computed(() => {
  const result = [];

  for (let r = 0; r < inventory.value.height; r++) {
    for (let c = 0; c < inventory.value.width; c++) {
      const slotIndex = r * inventory.value.width + c;
      const cell = localOccupiedCells.value[slotIndex];

      const connections = {
        top: false,
        right: false,
        bottom: false,
        left: false
      };

      if (cell) {
        const inventoryItemId = cell.inventoryItemId;

        const topIndex = (r - 1) * inventory.value.width + c;
        const rightIndex = r * inventory.value.width + (c + 1);
        const bottomIndex = (r + 1) * inventory.value.width + c;
        const leftIndex = r * inventory.value.width + (c - 1);

        if (r > 0 && localOccupiedCells.value[topIndex] &&
            localOccupiedCells.value[topIndex].inventoryItemId === inventoryItemId) {
          connections.top = true;
        }

        if (c < inventory.value.width - 1 && localOccupiedCells.value[rightIndex] &&
            localOccupiedCells.value[rightIndex].inventoryItemId === inventoryItemId) {
          connections.right = true;
        }

        if (r < inventory.value.height - 1 && localOccupiedCells.value[bottomIndex] &&
            localOccupiedCells.value[bottomIndex].inventoryItemId === inventoryItemId) {
          connections.bottom = true;
        }

        if (c > 0 && localOccupiedCells.value[leftIndex] &&
            localOccupiedCells.value[leftIndex].inventoryItemId === inventoryItemId) {
          connections.left = true;
        }
      }

      result.push({
        row: r,
        col: c,
        index: slotIndex,
        isOccupied: !!cell,
        itemId: cell ? cell.id : null,
        inventoryItemId: cell ? cell.inventoryItemId : null,
        isPrimary: cell && cell.inventoryItemId === slotIndex,
        connections
      });
    }
  }

  return result;
});

const mouseSlotPosition = computed(() => {
  if (currentMouseSlot.value === null || !inventory.value) {
    return null;
  }

  const invWidth = inventory.value.width || 1;
  const row = Math.floor(currentMouseSlot.value / invWidth);
  const col = currentMouseSlot.value % invWidth;

  return {
    gridRowStart: row + 1,
    gridColumnStart: col + 1,
    gridRowEnd: row + 2,
    gridColumnEnd: col + 2
  };
});

const getItemDefinition = (itemId) => {
  if (!itemId) return null;
  return allItems.value.find(item => item.id === itemId);
};

const startDrag = (e, inventoryItemId) => {
  e.preventDefault();

  const item = localItems.value.find(item => item.inventoryItemId === inventoryItemId);
  if (!item) return;

  dragging.value = true;
  draggedItemId.value = inventoryItemId;
  draggedItemData.value = itemGroups.value[inventoryItemId];

  // Get mouse/touch position
  const clientX = e.clientX || (e.touches && e.touches[0] ? e.touches[0].clientX : 0);
  const clientY = e.clientY || (e.touches && e.touches[0] ? e.touches[0].clientY : 0);

  const element = e.currentTarget;
  const rect = element.getBoundingClientRect();

  // Calculate offset from the top-left corner of the item
  dragOffset.value = {
    x: clientX - rect.left,
    y: clientY - rect.top
  };

  dragStartPos.value = { x: clientX, y: clientY };
  dragCurrentPos.value = { x: clientX, y: clientY };

  window.addEventListener('mousemove', onDragMove);
  window.addEventListener('touchmove', onDragMove, { passive: false });
  window.addEventListener('mouseup', onDragEnd);
  window.addEventListener('touchend', onDragEnd);
};

const onDragMove = (e) => {
  if (!dragging.value) return;

  e.preventDefault();

  const clientX = e.clientX || e.touches?.[0]?.clientX || 0;
  const clientY = e.clientY || e.touches?.[0]?.clientY || 0;

  dragCurrentPos.value = {x: clientX, y: clientY};

  calculateDropSlot(clientX, clientY);
};

const calculateDropSlot = (clientX, clientY) => {
  const gridElement = document.querySelector('.inventory-grid');
  if (!gridElement) return;

  const gridRect = gridElement.getBoundingClientRect();

  if (
      clientX < gridRect.left ||
      clientX > gridRect.right ||
      clientY < gridRect.top ||
      clientY > gridRect.bottom
  ) {
    currentMouseSlot.value = null;
    isValidDrop.value = false;
    return;
  }

  const slotSize = 64;
  const gap = 8;
  const totalSlotSize = slotSize + gap;

  const gridX = clientX - gridRect.left;
  const gridY = clientY - gridRect.top;

  const col = Math.floor(gridX / totalSlotSize);
  const row = Math.floor(gridY / totalSlotSize);

  const invWidth = inventory.value?.width || 1;
  const invHeight = inventory.value?.height || 1;

  // Check if cursor is within grid bounds
  if (row < 0 || col < 0 || row >= invHeight || col >= invWidth) {
    currentMouseSlot.value = null;
    isValidDrop.value = false;
    return;
  }

  const mouseSlotIndex = row * invWidth + col;

  if (currentMouseSlot.value !== mouseSlotIndex) {
    currentMouseSlot.value = mouseSlotIndex;

    const item = localItems.value.find(item => item.inventoryItemId === draggedItemId.value);
    if (item) {
      const itemDef = getItemDefinition(item.id);
      if (itemDef) {
        // Apply the offset from where the user clicked to determine target slot
        const xOffsetSlots = Math.floor(dragOffset.value.x / slotSize);
        const yOffsetSlots = Math.floor(dragOffset.value.y / slotSize);

        const targetCol = Math.max(0, col - xOffsetSlots);
        const targetRow = Math.max(0, row - yOffsetSlots);

        // Check if any part of the item would go out of bounds
        let outOfBounds = false;
        if (itemDef.shape) {
          const dimensions = InventoryUtils.getActualDimensions(itemDef.shape);

          if (targetRow + dimensions.height > invHeight ||
              targetCol + dimensions.width > invWidth) {
            outOfBounds = true;
          }

          if (col - xOffsetSlots < 0 || row - yOffsetSlots < 0) {
            outOfBounds = true;
          }
        }

        if (outOfBounds) {
          isValidDrop.value = false;
        } else {
          const tempInventory = {
            width: invWidth,
            height: invHeight,
            items: inventory.value?.items?.filter(i => i.cells[0] !== item.inventoryItemId) || []
          };

          isValidDrop.value = InventoryUtils.canPlaceItem(
              {shape: itemDef.shape},
              targetRow,
              targetCol,
              tempInventory
          );
        }
      }
    }
  }
};

const updateClientSideInventory = (item, newSlot) => {
  const invWidth = inventory.value?.width || 1;

  // Apply offset to calculate the actual placement slot
  const xOffsetSlots = Math.floor(dragOffset.value.x / 64);
  const yOffsetSlots = Math.floor(dragOffset.value.y / 64);

  const currentRow = Math.floor(newSlot / invWidth);
  const currentCol = newSlot % invWidth;

  const targetRow = Math.max(0, currentRow - yOffsetSlots);
  const targetCol = Math.max(0, currentCol - xOffsetSlots);

  // Ensure we stay within grid bounds
  const finalRow = Math.min(targetRow, inventory.value.height - 1);
  const finalCol = Math.min(targetCol, invWidth - 1);

  const placementSlot = finalRow * invWidth + finalCol;

  // Get current positions
  const oldRow = Math.floor(item.inventoryItemId / invWidth);
  const oldCol = item.inventoryItemId % invWidth;
  const newRow = finalRow;
  const newCol = finalCol;

  // Calculate movement delta
  const rowDelta = newRow - oldRow;
  const colDelta = newCol - oldCol;

  // Create a new occupiedCells map without the moved item
  const newOccupiedCells = {};
  Object.entries(localOccupiedCells.value).forEach(([index, cell]) => {
    if (cell.inventoryItemId !== item.inventoryItemId) {
      newOccupiedCells[index] = cell;
    }
  });

  // Calculate new cells for the item
  const newCells = [];
  item.cells.forEach(cellIndex => {
    const cellRow = Math.floor(cellIndex / invWidth);
    const cellCol = cellIndex % invWidth;
    const newCellIndex = (cellRow + rowDelta) * invWidth + (cellCol + colDelta);

    // Add to new cells array
    newCells.push(newCellIndex);

    // Mark as occupied in the map
    newOccupiedCells[newCellIndex] = {
      id: item.id,
      inventoryItemId: item.inventoryItemId + (rowDelta * invWidth) + colDelta
    };
  });

  // Update local items
  const updatedItems = localItems.value.map(localItem => {
    if (localItem.inventoryItemId === item.inventoryItemId) {
      const newItemId = item.inventoryItemId + (rowDelta * invWidth) + colDelta;
      return {
        ...localItem,
        inventoryItemId: newItemId,
        row: newRow,
        col: newCol,
        cells: newCells
      };
    }
    return localItem;
  });

  // Update the refs
  localOccupiedCells.value = newOccupiedCells;
  localItems.value = updatedItems;

  // Update parent component
  emit('update:occupiedCells', newOccupiedCells);
  emit('update:items', updatedItems);
  emit('item-moved', item, placementSlot); // Pass the actual placement slot

  return {
    localItem: updatedItems.find(i => i.id === item.id),
    newCells
  };
};

const onDragEnd = async () => {
  if (!dragging.value) return;

  window.removeEventListener('mousemove', onDragMove);
  window.removeEventListener('touchmove', onDragMove);
  window.removeEventListener('mouseup', onDragEnd);
  window.removeEventListener('touchend', onDragEnd);

  if (currentMouseSlot.value !== null && isValidDrop.value) {
    try {
      const item = localItems.value.find(item => item.inventoryItemId === draggedItemId.value);
      const invWidth = inventory.value?.width || 1;

      // Apply offset to calculate the actual placement slot
      const xOffsetSlots = Math.floor(dragOffset.value.x / 64);
      const yOffsetSlots = Math.floor(dragOffset.value.y / 64);

      const currentRow = Math.floor(currentMouseSlot.value / invWidth);
      const currentCol = currentMouseSlot.value % invWidth;

      const targetRow = Math.max(0, currentRow - yOffsetSlots);
      const targetCol = Math.max(0, currentCol - xOffsetSlots);

      const placementSlot = targetRow * invWidth + targetCol;

      if (item && (item.row !== targetRow || item.col !== targetCol)) {
        const inventoryItem = inventory.value.items.find(
            invItem => invItem.cells[0] === draggedItemId.value
        );

        if (inventoryItem) {
          // First update the client-side display immediately
          const updatedItemInfo = updateClientSideInventory(item, currentMouseSlot.value);

          // Then send the update to the server
          const success = await InventoryApi.move(inventoryItem, placementSlot);

          if (success) {
            // Update the item position in the store without fetching everything again
            inventoryManager.updateItemPosition(item, placementSlot, invWidth);
          } else {
            // If server request failed, revert the client-side changes
            console.error('Server rejected the move - reverting UI');
            updateClientSideInventory(updatedItemInfo.localItem, item.inventoryItemId);
          }
        }
      }
    } catch (error) {
      console.error('Error moving item:', error);
    }
  }

  dragging.value = false;
  draggedItemId.value = null;
  draggedItemData.value = null;
  currentMouseSlot.value = null;
  isValidDrop.value = false;
};

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onDragMove);
  window.removeEventListener('touchmove', onDragMove);
  window.removeEventListener('mouseup', onDragEnd);
  window.removeEventListener('touchend', onDragEnd);
});
</script>

<template>
  <div class="grid-slots-wrapper">
    <GridSlot
        v-for="slot in slotData"
        :key="`slot-${slot.index}`"
        :row="slot.row"
        :col="slot.col"
        :index="slot.index"
        :is-occupied="slot.isOccupied"
        :connections="slot.connections"
    />

    <!-- Drop target indicator -->
    <div
        v-if="currentMouseSlot !== null && mouseSlotPosition"
        class="drop-indicator"
        :class="{ 'valid-drop': isValidDrop, 'invalid-drop': !isValidDrop }"
        :style="mouseSlotPosition"
    ></div>

    <!-- Item containers -->
    <div
        v-for="(item, key) in itemGroups"
        :key="`item-${key}`"
        class="item-container"
        :class="{ 'dragging': dragging && draggedItemId === parseInt(key) }"
        :style="{
        gridRowStart: item.row + 1,
        gridColumnStart: item.col + 1,
        gridRowEnd: `span ${item.height}`,
        gridColumnEnd: `span ${item.width}`,
        opacity: dragging && draggedItemId === parseInt(key) ? '0.3' : '1'
      }"
        @mousedown="startDrag($event, parseInt(key))"
        @touchstart="startDrag($event, parseInt(key))"
    >
      <img
          :src="item.imagePath"
          :alt="item.id"
          class="item-image"
          draggable="false"
      />
    </div>

    <!-- Dragged item ghost -->
    <div
        v-if="dragging && draggedItemData"
        class="dragged-item"
        :style="{
          position: 'fixed',
          left: `${dragCurrentPos.x - dragOffset.x}px`,
          top: `${dragCurrentPos.y - dragOffset.y}px`,
          width: `${draggedItemData.width * 64}px`,
          height: `${draggedItemData.height * 64}px`,
          zIndex: 1000,
          opacity: 0.8,
          pointerEvents: 'none'
        }"
    >
      <img
          :src="draggedItemData.imagePath"
          :alt="draggedItemData.id"
          class="item-image"
          draggable="false"
      />
    </div>
  </div>
</template>

<style scoped>
.grid-slots-wrapper {
  display: contents;
  position: relative;
}

.item-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  z-index: 10;
  cursor: grab;
  transition: opacity 0.2s ease;
}

.item-image {
  image-rendering: pixelated;
  width: 100%;
  height: 100%;
  object-fit: contain;
  position: absolute;
  pointer-events: none;
}

.dragging {
  opacity: 0.3;
}

.dragged-item {
  position: fixed;
  z-index: 1000;
  pointer-events: none;
  opacity: 0.8;
}

.drop-indicator {
  position: absolute;
  width: 64px;
  height: 64px;
  z-index: 5;
  border-radius: 24px;
  pointer-events: none;
  background-color: rgba(255, 255, 255, 0.2);
}

.valid-drop {
  background-color: rgba(0, 255, 0, 0.2);
  border: 2px solid rgba(0, 255, 0, 0.5);
}

.invalid-drop {
  background-color: rgba(255, 0, 0, 0.2);
  border: 2px solid rgba(255, 0, 0, 0.5);
}
</style>