<template>
  <div class="inventory-container" :class="{ 'is-loading': loading }"
       @mousemove="onMouseMove"
       @dragover.prevent="onDragOver">
    <div class="inventory-header">
      <h2>Inventory</h2>
    </div>

    <div v-if="isGridReady"
         class="inventory-grid"
         :style="gridStyle"
         @drop.prevent="onGridDrop"
         @touchmove.prevent
    >
      <template v-for="(row, y) in grid" :key="`row-${y}`">
        <grid-cell
            v-for="(cell, x) in row"
            :key="`cell-${x}-${y}`"
            :x="x"
            :y="y"
            :is-occupied="cell !== null"
            :is-origin="cell?.isOrigin"
            :item-ref="cell"
            :inventory-manager="inventoryStore.state.inventoryManager"
            :is-current-target="x === currentTargetX && y === currentTargetY"
            :is-dragging-active="isDraggingActive"
            :current-drag-item-id="currentDragItemId"
            @item-dropped="onItemDropped"
        >
          <inventory-item
              v-if="cell && cell.isOrigin"
              :item="getItemByInstanceId(cell.instanceId)"
              :position="{ x, y }"
              :cell-size="cellSize"
              :inventory-bounds="inventoryBounds"
              @touch-drag-start="onTouchDragStart"
              @touch-drag-move="onTouchDragMove"
              @touch-drag-end="onTouchDragEnd"
              @drag-start="onItemDragStart"
              @item-deleted="onItemDeleted"
          />
        </grid-cell>
      </template>
    </div>

    <div v-if="!isGridReady && !loading" class="empty-grid-message">
      Loading inventory...
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
    </div>

  </div>
</template>

<script>
import {computed, onBeforeUnmount, onMounted, ref, toRefs, watch} from 'vue';
import GridCell from './GridCell.vue';
import InventoryItem from './InventoryItem.vue';
import inventoryStore from './store/inventoryStore';

export default {
  name: 'Inventory',

  components: {
    GridCell,
    InventoryItem
  },

  props: {
    cellSize: {
      type: Number,
      default: 60
    }
  },

  setup(props) {
    const {loading, error} = toRefs(inventoryStore.state);
    const grid = inventoryStore.grid;
    const isGridReady = ref(false);

    // Track current dragging state
    const isDraggingActive = ref(false);
    const currentDragItem = ref(null);
    const currentDragItemId = ref(null);
    const dragStartPosition = ref(null);
    const clickedCell = ref(null);

    // Track current target position
    const currentTargetX = ref(-1);
    const currentTargetY = ref(-1);

    // Get inventory bounds for tooltip positioning
    const inventoryBounds = computed(() => {
      if (!inventoryStore.state.inventoryManager) return { width: 0, height: 0 };

      return {
        width: inventoryStore.state.inventoryManager.width * props.cellSize,
        height: inventoryStore.state.inventoryManager.height * props.cellSize
      };
    });

    const gridStyle = computed(() => {
      if (!inventoryStore.state.inventoryManager) return {};

      const {width, height} = inventoryStore.state.inventoryManager;
      return {
        gridTemplateColumns: `repeat(${width}, ${props.cellSize}px)`,
        gridTemplateRows: `repeat(${height}, ${props.cellSize}px)`,
        gap: '0px'
      };
    });

    // Watch for when the inventory manager is ready
    watch(() => inventoryStore.state.inventoryManager, (newVal) => {
      isGridReady.value = newVal && grid.value && grid.value.length > 0;
    }, {immediate: true});

    // Also watch the grid to update readiness
    watch(() => grid.value, (newVal) => {
      isGridReady.value = inventoryStore.state.inventoryManager && newVal && newVal.length > 0;
    }, {immediate: true});

    onMounted(() => {
      inventoryStore.openInventory().then(() => {
        isGridReady.value = inventoryStore.state.inventoryManager && grid.value && grid.value.length > 0;
      });
      window.addEventListener('beforeunload', handleBeforeUnload);
    });

    onBeforeUnmount(() => {
      inventoryStore.closeInventory();
      window.removeEventListener('beforeunload', handleBeforeUnload);
    });

    const refreshInventory = () => {
      inventoryStore.fetchInventory(true).then(() => {
        isGridReady.value = true;
      });
    };

    const getItemByInstanceId = (instanceId) => {
      if (!inventoryStore.state.inventoryManager) return null;
      return inventoryStore.state.inventoryManager.getItemByInstanceId(instanceId);
    };

    // Track mouse movement to update target cell
    const onMouseMove = (event) => {
      if (!isDraggingActive.value || !currentDragItem.value) return;

      // Get grid position
      const gridElement = event.currentTarget.querySelector('.inventory-grid');
      if (!gridElement) return;

      const rect = gridElement.getBoundingClientRect();

      // Calculate which cell the mouse is over
      const mouseX = event.clientX - rect.left;
      const mouseY = event.clientY - rect.top;

      const cellX = Math.floor(mouseX / props.cellSize);
      const cellY = Math.floor(mouseY / props.cellSize);

      // Ensure within grid bounds
      if (cellX >= 0 && cellX < inventoryStore.state.inventoryManager.width &&
          cellY >= 0 && cellY < inventoryStore.state.inventoryManager.height) {
        currentTargetX.value = cellX;
        currentTargetY.value = cellY;
      }
    };

    // Handle dragover to update target position
    const onDragOver = (event) => {
      if (!isDraggingActive.value) return;

      // Similar calculation as onMouseMove
      const gridElement = event.currentTarget.querySelector('.inventory-grid');
      if (!gridElement) return;

      const rect = gridElement.getBoundingClientRect();

      const mouseX = event.clientX - rect.left;
      const mouseY = event.clientY - rect.top;

      const cellX = Math.floor(mouseX / props.cellSize);
      const cellY = Math.floor(mouseY / props.cellSize);

      if (cellX >= 0 && cellX < inventoryStore.state.inventoryManager.width &&
          cellY >= 0 && cellY < inventoryStore.state.inventoryManager.height) {
        currentTargetX.value = cellX;
        currentTargetY.value = cellY;
      }
    };

    // Start tracking when an item starts being dragged
    const onItemDragStart = (data) => {
      isDraggingActive.value = true;
      currentDragItem.value = data.item;
      currentDragItemId.value = data.item.instanceId;
      clickedCell.value = data.clickedCell;
      dragStartPosition.value = data.position;
    };

    // Handle drop on the entire grid
    const onGridDrop = (event) => {
      if (!isDraggingActive.value || !currentDragItem.value) {
        resetDragState();
        return;
      }

      try {
        // Calculate the new position based on original grabbed cell and current target
        const item = currentDragItem.value;
        const grabbedCellX = clickedCell.value?.x || 0;
        const grabbedCellY = clickedCell.value?.y || 0;

        const newX = currentTargetX.value - grabbedCellX;
        const newY = currentTargetY.value - grabbedCellY;

        // Make sure position is valid
        const adjustedX = Math.max(0, Math.min(
            inventoryStore.state.inventoryManager.width - item.width,
            newX
        ));
        const adjustedY = Math.max(0, Math.min(
            inventoryStore.state.inventoryManager.height - item.height,
            newY
        ));

        // Move the item
        inventoryStore.moveItem(item.instanceId, { x: adjustedX, y: adjustedY });

      } catch (error) {
        console.error('Error handling grid drop:', error);
      }

      // Reset drag state
      resetDragState();
    };

    const resetDragState = () => {
      isDraggingActive.value = false;
      currentDragItem.value = null;
      currentDragItemId.value = null;
      clickedCell.value = null;
      dragStartPosition.value = null;
      currentTargetX.value = -1;
      currentTargetY.value = -1;
    };

    const onItemDropped = ({instanceId, itemId, position, clickedCell}) => {
      // If we don't have instanceId (for backward compatibility)
      if (!instanceId && itemId) {
        // Find the first item with matching itemId
        const items = inventoryStore.state.inventoryManager.findItemsByItemId(itemId);
        if (items.length > 0) {
          instanceId = items[0].instanceId;
        }
      }

      if (!instanceId) {
        console.error("Cannot move item: missing instanceId");
        return;
      }

      // If we have clicked cell info, adjust the position
      if (clickedCell) {
        // Calculate target position accounting for which part of the item was grabbed
        position = {
          x: position.x - clickedCell.x,
          y: position.y - clickedCell.y
        };

        // Make sure the position is within bounds
        const item = getItemByInstanceId(instanceId);
        if (item) {
          position.x = Math.max(0, Math.min(
              inventoryStore.state.inventoryManager.width - item.width,
              position.x
          ));
          position.y = Math.max(0, Math.min(
              inventoryStore.state.inventoryManager.height - item.height,
              position.y
          ));
        }
      }

      inventoryStore.moveItem(instanceId, position);
    };

    const onItemDeleted = ({instanceId}) => {
      if (window.confirm(`Do you want to remove this item?`)) {
        inventoryStore.removeItem(instanceId);
      }
    };

    const onTouchDragStart = ({instanceId, position}) => {
      currentDragItem.value = {
        instanceId,
        originalPosition: position
      };
      dragStartPosition.value = position;
    };

    const onTouchDragMove = ({instanceId, deltaX, deltaY}) => {
      // Touch drag move is now handled with a visual ghost element
    };

    const onTouchDragEnd = ({instanceId, deltaX, deltaY, position}) => {
      if (!currentDragItem.value) return;

      // Calculate new position based on delta movement
      const cellWidth = props.cellSize;
      const cellHeight = props.cellSize;

      const xDelta = Math.round(deltaX / cellWidth);
      const yDelta = Math.round(deltaY / cellHeight);

      const newPosition = {
        x: Math.max(0, Math.min(
            inventoryStore.state.inventoryManager.width - 1,
            position.x + xDelta
        )),
        y: Math.max(0, Math.min(
            inventoryStore.state.inventoryManager.height - 1,
            position.y + yDelta
        ))
      };

      // Only move if the position actually changed
      if (newPosition.x !== position.x || newPosition.y !== position.y) {
        inventoryStore.moveItem(instanceId, newPosition);
      }

      // Reset drag state
      currentDragItem.value = null;
      dragStartPosition.value = null;
    };

    const handleBeforeUnload = (event) => {
      if (loading.value) {
        const message = 'Changes are being saved. Are you sure you want to leave?';
        event.returnValue = message;
        return message;
      }
    };

    return {
      inventoryStore,
      loading,
      error,
      gridStyle,
      grid,
      isGridReady,
      refreshInventory,
      getItemByInstanceId,
      onItemDropped,
      onItemDeleted,
      onTouchDragStart,
      onTouchDragMove,
      onTouchDragEnd,
      handleBeforeUnload,
      isDraggingActive,
      currentDragItemId,
      currentTargetX,
      currentTargetY,
      onMouseMove,
      onDragOver,
      onItemDragStart,
      onGridDrop,
      inventoryBounds
    };
  }
};
</script>

<style scoped>
.inventory-container {
  width: auto;
  max-width: 600px;
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

.inventory-header {
  display: flex;
  justify-content: center;
  align-items: center;
}

.inventory-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

.inventory-grid {
  display: grid;
  gap: 0;
  background-color: #333;
  border: 1px solid #555;
  border-radius: 8px;
  overflow: hidden;
  width: fit-content;
  margin: 0 auto;
}

.empty-grid-message {
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