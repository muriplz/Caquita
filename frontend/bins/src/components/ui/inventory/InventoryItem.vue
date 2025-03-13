<template>
  <div
      class="inventory-item"
      :class="{
        'is-dragging': isDragging,
        [`rarity-${item?.rarity?.toLowerCase() || 'common'}`]: true
      }"
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
      <img
          v-if="item.imageUrl"
          :src="item.imageUrl"
          :alt="item.name"
          class="item-image"
          @error="handleImageError"
      />
      <div v-else class="item-placeholder" :style="{ background: generateItemColor(item?.itemId || '0') }">
        {{ item?.name ? item.name.substring(0, 2).toUpperCase() : 'IT' }}
      </div>
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
      default: () => ({x: 0, y: 0})
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
      touchCurrentY: 0,
      touchGhostElement: null,
      imageError: false
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
        backgroundColor: this.generateItemColor(this.item.itemId || '0', 0.8),
        position: 'absolute',
        top: '0',
        left: '0',
        margin: '0',
        padding: '0',
        boxSizing: 'border-box'
      };
    }
  },

  methods: {
    handleImageError() {
      this.imageError = true;
    },

    onDragStart(event) {
      if (!this.item) return;

      this.isDragging = true;

      const rect = event.currentTarget.getBoundingClientRect();
      const clickOffsetX = event.clientX - rect.left;
      const clickOffsetY = event.clientY - rect.top;

      const cellX = Math.floor(clickOffsetX / this.cellSize);
      const cellY = Math.floor(clickOffsetY / this.cellSize);

      const dragData = {
        instanceId: this.item.instanceId,
        itemId: this.item.itemId,
        width: this.item.width,
        height: this.item.height,
        clickedCell: {x: cellX, y: cellY}
      };

      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('application/json', JSON.stringify(dragData));

      const dragPreview = document.createElement('div');
      dragPreview.className = 'drag-preview';

      const width = (this.item.width || 1) * this.cellSize;
      const height = (this.item.height || 1) * this.cellSize;

      dragPreview.style.width = `${width}px`;
      dragPreview.style.height = `${height}px`;
      dragPreview.style.backgroundColor = this.generateItemColor(this.item.itemId || '0', 0.6);
      dragPreview.style.border = '2px dashed #fff';
      dragPreview.style.borderRadius = '4px';
      dragPreview.style.display = 'flex';
      dragPreview.style.alignItems = 'center';
      dragPreview.style.justifyContent = 'center';
      dragPreview.style.overflow = 'hidden';

      if (this.item.imageUrl && !this.imageError) {
        const img = document.createElement('img');
        img.src = this.item.imageUrl;
        img.style.width = '100%';
        img.style.height = '100%';
        img.style.objectFit = 'contain';
        img.style.imageRendering = 'pixelated';

        dragPreview.appendChild(img);
      } else {
        dragPreview.textContent = this.item.name || 'Item';
      }

      document.body.appendChild(dragPreview);

      event.dataTransfer.setDragImage(
          dragPreview,
          width / 2,
          height / 2
      );

      setTimeout(() => {
        document.body.removeChild(dragPreview);
      }, 0);
    },

    onDragEnd() {
      this.isDragging = false;
    },

    onTouchStart(event) {
      if (!this.item) return;

      event.preventDefault();

      const touch = event.touches[0];
      this.touchStartX = touch.clientX;
      this.touchStartY = touch.clientY;
      this.touchCurrentX = touch.clientX;
      this.touchCurrentY = touch.clientY;

      this.isDragging = true;

      this.createTouchGhost();

      this.$emit('touch-drag-start', {
        instanceId: this.item.instanceId,
        itemId: this.item.itemId,
        position: this.position
      });
    },

    createTouchGhost() {
      this.touchGhostElement = document.createElement('div');
      const ghost = this.touchGhostElement;

      ghost.className = 'touch-drag-ghost';
      ghost.style.width = `${(this.item.width || 1) * this.cellSize}px`;
      ghost.style.height = `${(this.item.height || 1) * this.cellSize}px`;
      ghost.style.backgroundColor = this.generateItemColor(this.item.itemId || '0', 0.6);
      ghost.style.border = '2px dashed #fff';
      ghost.style.borderRadius = '4px';
      ghost.style.position = 'fixed';
      ghost.style.zIndex = '9999';
      ghost.style.pointerEvents = 'none';
      ghost.style.opacity = '0.7';
      ghost.style.transform = 'translate(-50%, -50%)';
      ghost.style.left = `${this.touchCurrentX}px`;
      ghost.style.top = `${this.touchCurrentY}px`;
      ghost.style.display = 'flex';
      ghost.style.alignItems = 'center';
      ghost.style.justifyContent = 'center';
      ghost.style.overflow = 'hidden';

      if (this.item.imageUrl && !this.imageError) {
        const img = document.createElement('img');
        img.src = this.item.imageUrl;
        img.style.width = '100%';
        img.style.height = '100%';
        img.style.objectFit = 'contain';
        img.style.imageRendering = 'pixelated';

        ghost.appendChild(img);
      } else {
        ghost.textContent = this.item.name || 'Item';
      }

      document.body.appendChild(ghost);
    },

    onTouchMove(event) {
      if (!this.item || !this.isDragging) return;

      event.preventDefault();

      const touch = event.touches[0];
      this.touchCurrentX = touch.clientX;
      this.touchCurrentY = touch.clientY;

      this.updateTouchGhostPosition();

      this.$emit('touch-drag-move', {
        instanceId: this.item.instanceId,
        itemId: this.item.itemId,
        deltaX: this.touchCurrentX - this.touchStartX,
        deltaY: this.touchCurrentY - this.touchStartY,
        position: this.position
      });
    },

    updateTouchGhostPosition() {
      if (this.touchGhostElement) {
        this.touchGhostElement.style.left = `${this.touchCurrentX}px`;
        this.touchGhostElement.style.top = `${this.touchCurrentY}px`;
      }
    },

    onTouchEnd(event) {
      if (!this.item || !this.isDragging) return;

      event.preventDefault();

      const deltaX = this.touchCurrentX - this.touchStartX;
      const deltaY = this.touchCurrentY - this.touchStartY;

      this.removeTouchGhost();
      this.isDragging = false;

      this.$emit('touch-drag-end', {
        instanceId: this.item.instanceId,
        itemId: this.item.itemId,
        deltaX,
        deltaY,
        position: this.position
      });
    },

    removeTouchGhost() {
      if (this.touchGhostElement && this.touchGhostElement.parentNode) {
        document.body.removeChild(this.touchGhostElement);
        this.touchGhostElement = null;
      }
    },

    onClick() {
      if (!this.item) return;
      this.$emit('item-clicked', {
        instanceId: this.item.instanceId,
        itemId: this.item.itemId,
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
  z-index: 1;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
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
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
}

.item-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 1.2rem;
  font-weight: bold;
  color: rgba(0, 0, 0, 0.7);
  border-radius: 4px;
}

.item-image {
  image-rendering: pixelated;
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.rarity-common {
  border: 2px solid #a0a0a0;
}

.rarity-uncommon {
  border: 2px solid #44bd32;
}

.rarity-rare {
  border: 2px solid #0097e6;
}

.rarity-epic {
  border: 2px solid #8c7ae6;
}

.rarity-legendary {
  border: 2px solid #e1b12c;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1) 0%, rgba(255, 215, 0, 0.3) 100%);
}
</style>