<template>
  <div class="inventory-container" :class="{ 'is-loading': loading }">
    <div class="inventory-header">
      <h2>Inventory</h2>
      <div class="inventory-actions">
        <button
            class="refresh-button"
            @click="refreshInventory"
            :disabled="loading"
        >
          Refresh
        </button>
      </div>
    </div>

    <div v-if="isGridReady"
         class="inventory-grid"
         :style="gridStyle"
         @dragover.prevent
         @drop.prevent
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
            @item-dropped="onItemDropped"
        >
          <inventory-item
              v-if="cell && cell.isOrigin"
              :item="getItemById(cell.itemId)"
              :position="{ x, y }"
              :cell-size="cellSize"
              @item-clicked="onItemClicked"
              @touch-drag-start="onTouchDragStart"
              @touch-drag-move="onTouchDragMove"
              @touch-drag-end="onTouchDragEnd"
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
import { computed, onMounted, onBeforeUnmount, toRefs, ref, watch } from 'vue';
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
    const { loading, error } = toRefs(inventoryStore.state);
    const grid = inventoryStore.grid;
    const isGridReady = ref(false);

    // Track current dragging item
    const currentDragItem = ref(null);
    const dragStartPosition = ref(null);

    const gridStyle = computed(() => {
      if (!inventoryStore.state.inventoryManager) return {};

      const { width, height } = inventoryStore.state.inventoryManager;
      return {
        gridTemplateColumns: `repeat(${width}, ${props.cellSize}px)`,
        gridTemplateRows: `repeat(${height}, ${props.cellSize}px)`
      };
    });

    // Watch for when the inventory manager is ready
    watch(() => inventoryStore.state.inventoryManager, (newVal) => {
      isGridReady.value = newVal && grid.value && grid.value.length > 0;
    }, { immediate: true });

    // Also watch the grid to update readiness
    watch(() => grid.value, (newVal) => {
      isGridReady.value = inventoryStore.state.inventoryManager && newVal && newVal.length > 0;
    }, { immediate: true });

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

    const getItemById = (itemId) => {
      if (!inventoryStore.state.inventoryManager) return null;
      return inventoryStore.state.inventoryManager.getItem(itemId);
    };

    const onItemDropped = ({ itemId, position, clickedCell }) => {
      // If we have clicked cell info (from desktop drag), adjust the position
      if (clickedCell) {
        const item = getItemById(itemId);
        if (item) {
          // Adjust the position based on which cell within the item was clicked
          position = {
            x: Math.max(0, Math.min(
                inventoryStore.state.inventoryManager.width - item.width,
                position.x - clickedCell.x
            )),
            y: Math.max(0, Math.min(
                inventoryStore.state.inventoryManager.height - item.height,
                position.y - clickedCell.y
            ))
          };
        }
      }

      inventoryStore.moveItem(itemId, position);
    };

    const onItemClicked = ({ itemId }) => {
      const item = getItemById(itemId);
      if (window.confirm(`Do you want to remove ${item?.name || 'this item'}?`)) {
        inventoryStore.removeItem(itemId);
      }
    };

    const onTouchDragStart = ({ itemId, position }) => {
      currentDragItem.value = {
        itemId,
        originalPosition: position
      };
      dragStartPosition.value = position;
    };

    const onTouchDragMove = ({ itemId, deltaX, deltaY }) => {
      // Touch drag move is now handled with a visual ghost element
    };

    const onTouchDragEnd = ({ itemId, deltaX, deltaY, position }) => {
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
        inventoryStore.moveItem(itemId, newPosition);
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
      getItemById,
      onItemDropped,
      onItemClicked,
      onTouchDragStart,
      onTouchDragMove,
      onTouchDragEnd,
      handleBeforeUnload
    };
  }
};
</script>

<style scoped>
.inventory-container {
  width: 90%;
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
  justify-content: space-between;
  align-items: center;
}

.inventory-header h2 {
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

.inventory-grid {
  display: grid;
  gap: 1px;
  background-color: #333;
  border: 1px solid #555;
  border-radius: 8px;
  overflow: hidden;
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