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
      required: true
    },
    y: {
      type: Number,
      required: true
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
      required: true
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
        const itemData = JSON.parse(event.dataTransfer.getData('application/json') || '{}');

        if (!itemData.id) {
          this.isDropValid = false;
          return;
        }

        this.isDropValid = this.inventoryManager.canPlaceItem(
            itemData,
            { x: this.x, y: this.y },
            itemData.id
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
        const itemData = JSON.parse(event.dataTransfer.getData('application/json') || '{}');

        if (!itemData.id) return;

        this.$emit('item-dropped', {
          itemId: itemData.id,
          position: { x: this.x, y: this.y }
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
  border: 1px solid #ddd;
  box-sizing: border-box;
  background-color: #f9f9f9;
  transition: all 0.2s ease;
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