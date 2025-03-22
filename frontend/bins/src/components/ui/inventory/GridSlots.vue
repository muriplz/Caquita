<script setup>
import { ref, computed, onBeforeUnmount } from "vue";
import Store from "@/js/Store.js";
import GridSlot from "@/components/ui/inventory/GridSlot.vue";
import InventoryApi from "@/components/ui/inventory/js/InventoryApi.js";
import InventoryUtils from "@/components/ui/inventory/js/InventoryUtils.js";

const props = defineProps({
  occupiedCells: {
    type: Object,
    required: true
  },
  items: {
    type: Array,
    required: true
  }
});

const inventory = ref(Store.getInventory());
const allItems = ref(Store.getItems());
const dragging = ref(false);
const draggedItemId = ref(null);
const draggedItemData = ref(null);
const dragStartPos = ref({ x: 0, y: 0 });
const dragCurrentPos = ref({ x: 0, y: 0 });
const dragOffset = ref({ x: 0, y: 0 });
const dropTargetSlot = ref(null);
const isValidDrop = ref(false);

const itemGroups = computed(() => {
  const groups = {};

  props.items.forEach(item => {
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
      const cell = props.occupiedCells[slotIndex];

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

        if (r > 0 && props.occupiedCells[topIndex] &&
            props.occupiedCells[topIndex].inventoryItemId === inventoryItemId) {
          connections.top = true;
        }

        if (c < inventory.value.width - 1 && props.occupiedCells[rightIndex] &&
            props.occupiedCells[rightIndex].inventoryItemId === inventoryItemId) {
          connections.right = true;
        }

        if (r < inventory.value.height - 1 && props.occupiedCells[bottomIndex] &&
            props.occupiedCells[bottomIndex].inventoryItemId === inventoryItemId) {
          connections.bottom = true;
        }

        if (c > 0 && props.occupiedCells[leftIndex] &&
            props.occupiedCells[leftIndex].inventoryItemId === inventoryItemId) {
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

const getItemDefinition = (itemId) => {
  if (!itemId) return null;
  return allItems.value.find(item => item.id === itemId);
};

const startDrag = (e, inventoryItemId) => {
  e.preventDefault();

  const item = props.items.find(item => item.inventoryItemId === inventoryItemId);
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
    dropTargetSlot.value = null;
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

  const xOffsetSlots = Math.floor(dragOffset.value.x / slotSize);
  const yOffsetSlots = Math.floor(dragOffset.value.y / slotSize);

  const targetCol = Math.max(0, col - xOffsetSlots);
  const targetRow = Math.max(0, row - yOffsetSlots);

  const invWidth = inventory.value?.width || 1;
  const invHeight = inventory.value?.height || 1;

  const boundedCol = Math.min(targetCol, invWidth - 1);
  const boundedRow = Math.min(targetRow, invHeight - 1);

  const slotIndex = boundedRow * invWidth + boundedCol;

  if (dropTargetSlot.value !== slotIndex) {
    dropTargetSlot.value = slotIndex;

    const item = props.items.find(item => item.inventoryItemId === draggedItemId.value);
    if (item) {
      const itemDef = getItemDefinition(item.id);
      if (itemDef) {
        const tempInventory = {
          width: invWidth,
          height: invHeight,
          items: inventory.value?.items?.filter(i => i.cells[0] !== item.inventoryItemId) || []
        };

        isValidDrop.value = InventoryUtils.canPlaceItem(
            {shape: itemDef.shape},
            boundedRow,
            boundedCol,
            tempInventory
        );
      }
    }
  }
};

const onDragEnd = async () => {
  if (!dragging.value) return;

  window.removeEventListener('mousemove', onDragMove);
  window.removeEventListener('touchmove', onDragMove);
  window.removeEventListener('mouseup', onDragEnd);
  window.removeEventListener('touchend', onDragEnd);

  if (dropTargetSlot.value !== null && isValidDrop.value) {
    try {
      const item = props.items.find(item => item.inventoryItemId === draggedItemId.value);
      const invWidth = inventory.value?.width || 1;

      if (item && (item.row !== Math.floor(dropTargetSlot.value / invWidth) ||
          item.col !== dropTargetSlot.value % invWidth)) {

        const inventoryItem = inventory.value.items.find(
            invItem => invItem.cells[0] === draggedItemId.value
        );

        if (inventoryItem) {
          await InventoryApi.move(inventoryItem, dropTargetSlot.value);
          await Store.updateInventory();
        }
      }
    } catch (error) {
      console.error('Error moving item:', error);
    }
  }

  dragging.value = false;
  draggedItemId.value = null;
  draggedItemData.value = null;
  dropTargetSlot.value = null;
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
        :class="{ 'drop-target': dropTargetSlot === slot.index, 'valid-drop': isValidDrop && dropTargetSlot === slot.index, 'invalid-drop': !isValidDrop && dropTargetSlot === slot.index }"
    />

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

    <div
        v-if="dragging && draggedItemData"
        class="dragged-item"
        :style="{
      position: 'fixed',
      left: `${dragCurrentPos.x}px`,
      top: `${dragCurrentPos.y}px`,
      transform: 'translate(-50%, -50%)',
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

    <!-- Only show the drop preview when the drop is valid -->
    <div
        v-if="dragging && dropTargetSlot !== null && draggedItemData && isValidDrop"
        class="drop-preview valid-drop"
        :style="{
        gridRowStart: Math.floor(dropTargetSlot / (inventory.value?.width || 1)) + 1,
        gridColumnStart: dropTargetSlot % (inventory.value?.width || 1) + 1,
        gridRowEnd: `span ${draggedItemData.height}`,
        gridColumnEnd: `span ${draggedItemData.width}`
      }"
    ></div>
  </div>
</template>

<style scoped>
.grid-slots-wrapper {
  display: contents;
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

.drop-target {
  background-color: rgba(255, 255, 255, 0.1);
}

.drop-preview {
  position: relative;
  z-index: 15;
  border: 2px dashed;
  background-color: rgba(0, 255, 0, 0.2);
  pointer-events: none;
}

.valid-drop {
  border-color: rgba(0, 255, 0, 0.7);
}

.invalid-drop {
  border-color: rgba(255, 0, 0, 0.7);
  background-color: rgba(255, 0, 0, 0.2);
}
</style>