<template>
  <div
      class="grid-cell"
      :class="{
      'occupied': isOccupied,
      'drop-valid': isDropValid && !isOccupied,
      'drop-invalid': isDropTarget && !isDropValid,
      'is-origin': isOrigin
    }"
      @dragover.prevent="onDragOver"
      @dragleave="onDragLeave"
      @drop.prevent="onDrop"
      :data-x="x"
      :data-y="y"
  >
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'GridCell',

  props: {
    x: {
      type: Number,
      default: 0
    },
    y: {
      type: Number,
      default: 0
    },
    isOccupied: {
      type: Boolean,
      default: false
    },
    isOrigin: {
      type: Boolean,
      default: false
    },
    itemRef: {
      type: Object,
      default: null
    },
    inventoryManager: {
      type: Object,
      default: () => ({
        canPlaceItem: () => false
      })
    }
  },

  data() {
    return {
      isDropTarget: false,
      isDropValid: false
    };
  },

  methods: {
    onDragOver(event) {
      this.isDropTarget = true;

      try {
        const dataText = event.dataTransfer.getData('application/json');
        if (!dataText) {
          this.isDropValid = false;
          return;
        }

        const itemData = JSON.parse(dataText);

        // Check if we have instanceId or itemId for compatibility
        const item = itemData.instanceId
            ? this.inventoryManager.getItemByInstanceId(itemData.instanceId)
            : { width: itemData.width || 1, height: itemData.height || 1 };

        if (!item) {
          this.isDropValid = false;
          return;
        }

        this.isDropValid = this.inventoryManager.canPlaceItem(
            item,
            { x: this.x, y: this.y },
            itemData.instanceId
        );

        event.dataTransfer.dropEffect = this.isDropValid ? 'move' : 'none';
      } catch (error) {
        console.error('Error in drag over:', error);
        this.isDropValid = false;
      }
    },

    onDragLeave() {
      this.isDropTarget = false;
      this.isDropValid = false;
    },

    onDrop(event) {
      this.isDropTarget = false;
      this.isDropValid = false;

      try {
        const dataText = event.dataTransfer.getData('application/json');
        if (!dataText) return;

        const itemData = JSON.parse(dataText);

        // Pass all data to the parent component for processing
        this.$emit('item-dropped', {
          instanceId: itemData.instanceId,
          itemId: itemData.itemId,
          position: { x: this.x, y: this.y },
          clickedCell: itemData.clickedCell
        });
      } catch (error) {
        console.error('Error in drop:', error);
      }
    }
  }
};
</script>

<style scoped>
.grid-cell {
  position: relative;
  width: 100%;
  height: 100%;
  border: 1px solid #444; /* Darker border */
  box-sizing: border-box;
  background-color: #f9f9f9;
  transition: all 0.2s ease;
  margin: 0; /* Ensure no margin */
  padding: 0; /* Ensure no padding */
}

.grid-cell.occupied {
  background-color: rgba(200, 200, 200, 0.3);
}

.grid-cell.drop-valid {
  background-color: rgba(100, 255, 100, 0.2);
  border: 1px dashed #4caf50;
}

.grid-cell.drop-invalid {
  background-color: rgba(255, 100, 100, 0.2);
  border: 1px dashed #f44336;
}

.grid-cell.is-origin {
  background-color: rgba(220, 220, 220, 0.4);
}
</style>