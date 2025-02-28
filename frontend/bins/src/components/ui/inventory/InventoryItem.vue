<template>
  <div
      class="inventory-item"
      :class="{ 'is-dragging': isDragging }"
      :style="itemStyle"
      draggable="true"
      @dragstart="onDragStart"
      @dragend="onDragEnd"
      @click="onClick"
      @touchstart.prevent="onTouchStart"
      @touchmove.prevent="onTouchMove"
      @touchend.prevent="onTouchEnd"
  >
    <div class="item-content">
      <img v-if="item?.imageUrl" :src="item.imageUrl" :alt="item.name" class="item-image">
      <div v-else class="item-placeholder" :style="{ background: generateItemColor(item?.id || '0') }">
        {{ item?.name ? item.name.substring(0, 2).toUpperCase() : 'IT' }}
      </div>
      <div class="item-name">{{ item?.name || 'Item' }}</div>
      <div class="item-size">{{ item?.width || 1 }}x{{ item?.height || 1 }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'InventoryItem',

  props: {
    item: {
      type: Object,
      required: true
    },
    position: {
      type: Object,
      default: () => ({ x: 0, y: 0 })
    },
    cellSize: {
      type: Number,
      default: 60
    }
  },

  data() {
    return {
      isDragging: false,
      touchStartX: 0,
      touchStartY: 0,
      touchCurrentX: 0,
      touchCurrentY: 0
    };
  },

  computed: {
    itemStyle() {
      if (!this.item) return {};

      return {
        width: `${(this.item.width || 1) * this.cellSize}px`,
        height: `${(this.item.height || 1) * this.cellSize}px`,
        gridColumn: `span ${this.item.width || 1}`,
        gridRow: `span ${this.item.height || 1}`,
        backgroundColor: this.generateItemColor(this.item.id || '0', 0.8)
      };
    }
  },

  methods: {
    onDragStart(event) {
      if (!this.item) return;

      this.isDragging = true;
      console.log('Started dragging item:', this.item.id);

      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('application/json', JSON.stringify(this.item));

      const dragPreview = document.createElement('div');
      dragPreview.className = 'drag-preview';
      dragPreview.style.width = `${(this.item.width || 1) * this.cellSize}px`;
      dragPreview.style.height = `${(this.item.height || 1) * this.cellSize}px`;
      dragPreview.style.backgroundColor = this.generateItemColor(this.item.id || '0', 0.6);
      dragPreview.style.border = '2px dashed #666';
      dragPreview.style.borderRadius = '4px';
      dragPreview.style.display = 'flex';
      dragPreview.style.alignItems = 'center';
      dragPreview.style.justifyContent = 'center';
      dragPreview.textContent = this.item.name || 'Item';

      document.body.appendChild(dragPreview);
      event.dataTransfer.setDragImage(dragPreview, this.cellSize / 2, this.cellSize / 2);

      setTimeout(() => {
        document.body.removeChild(dragPreview);
      }, 0);
    },

    onDragEnd() {
      this.isDragging = false;
      if (this.item) {
        console.log('Finished dragging item:', this.item.id);
      }
    },

    onTouchStart(event) {
      if (!this.item) return;

      // Prevent default to stop scrolling
      event.preventDefault();

      // Store initial touch coordinates
      const touch = event.touches[0];
      this.touchStartX = touch.clientX;
      this.touchStartY = touch.clientY;
      this.touchCurrentX = touch.clientX;
      this.touchCurrentY = touch.clientY;

      // Simulate drag start
      this.isDragging = true;
      this.$emit('touch-drag-start', {
        itemId: this.item.id,
        position: this.position
      });
    },

    onTouchMove(event) {
      if (!this.item || !this.isDragging) return;

      // Prevent default to stop scrolling
      event.preventDefault();

      // Get current touch position
      const touch = event.touches[0];
      this.touchCurrentX = touch.clientX;
      this.touchCurrentY = touch.clientY;

      // Emit touch drag event with movement details
      this.$emit('touch-drag-move', {
        itemId: this.item.id,
        deltaX: this.touchCurrentX - this.touchStartX,
        deltaY: this.touchCurrentY - this.touchStartY,
        position: this.position
      });
    },

    onTouchEnd(event) {
      if (!this.item || !this.isDragging) return;

      // Prevent default
      event.preventDefault();

      // Calculate final movement
      const deltaX = this.touchCurrentX - this.touchStartX;
      const deltaY = this.touchCurrentY - this.touchStartY;

      // Simulate drag end
      this.isDragging = false;
      this.$emit('touch-drag-end', {
        itemId: this.item.id,
        deltaX,
        deltaY,
        position: this.position
      });
    },

    onClick() {
      if (!this.item) return;
      console.log('Clicked on item:', this.item.id);
      this.$emit('item-clicked', {
        itemId: this.item.id,
        position: this.position
      });
    },

    generateItemColor(id, alpha = 1) {
      const hash = Array.from(id.toString()).reduce(
          (hash, char) => ((hash << 5) - hash) + char.charCodeAt(0), 0
      );
      const hue = Math.abs(hash) % 360;
      return `hsla(${hue}, 70%, 80%, ${alpha})`;
    }
  }
};
</script>

<style scoped>
.inventory-item {
  position: relative;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: grab;
  transition: all 0.2s ease;
  user-select: none;
  z-index: 1;
}

.inventory-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
  z-index: 2;
}

.inventory-item.is-dragging {
  opacity: 0.6;
  transform: scale(0.95);
}

.item-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 4px;
  box-sizing: border-box;
}

.item-image {
  max-width: 100%;
  max-height: 60%;
  object-fit: contain;
}

.item-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 60%;
  font-size: 1.2rem;
  font-weight: bold;
  color: rgba(0, 0, 0, 0.7);
  border-radius: 4px;
}

.item-name {
  margin-top: 4px;
  font-size: 0.8rem;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  font-weight: bold;
}

.item-size {
  font-size: 0.7rem;
  color: rgba(0, 0, 0, 0.6);
}

@media (max-width: 768px) {
  .item-name {
    font-size: 0.7rem;
  }

  .item-size {
    font-size: 0.6rem;
  }
}
</style>