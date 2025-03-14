<template>
  <div
      class="inventory-item"
      :class="{
        'is-dragging': isDragging
      }"
      :style="itemStyle"
      draggable="true"
      @dragstart="onDragStart"
      @dragend="onDragEnd"
      @contextmenu.prevent="showItemTooltip"
      @mousedown="handleMouseDown"
      @touchstart="handleTouchStart"
      @touchmove.prevent="handleTouchMove"
      @touchend.prevent="handleTouchEnd"
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

    <ItemTooltip
        v-if="showTooltip"
        :item="item"
        :tooltipPosition="tooltipPosition"
    />
  </div>
</template>

<script>
import ItemTooltip from './ItemTooltip.vue';

export default {
  name: 'InventoryItem',

  components: {
    ItemTooltip
  },

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
    },
    inventoryBounds: {
      type: Object,
      default: () => ({width: 0, height: 0})
    }
  },

  data() {
    return {
      isDragging: false,
      touchStartX: 0,
      touchStartY: 0,
      touchMoved: false,
      touchCurrentX: 0,
      touchCurrentY: 0,
      touchGhostElement: null,
      imageError: false,
      showTooltip: false,
      tooltipPosition: {x: 0, y: 0},
      longPressTimer: null,
      longPressDuration: 500,
      mouseDownTime: 0,
      dragThreshold: 5,
      isDragStarted: false
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
        backgroundColor: 'white',
        position: 'absolute',
        top: '0',
        left: '0',
        margin: '0',
        padding: '0',
        boxSizing: 'border-box'
      };
    }
  },

  mounted() {
    document.addEventListener('click', this.handleGlobalClick);
    document.addEventListener('touchstart', this.handleGlobalTouch);

    // Listen for custom event to close other tooltips
    document.addEventListener('close-all-tooltips', this.handleCloseAllTooltips);
    document.addEventListener('open-tooltip', this.handleOtherTooltipOpened);
  },

  beforeUnmount() {
    document.removeEventListener('click', this.handleGlobalClick);
    document.removeEventListener('touchstart', this.handleGlobalTouch);
    document.removeEventListener('close-all-tooltips', this.handleCloseAllTooltips);
    document.removeEventListener('open-tooltip', this.handleOtherTooltipOpened);
    this.clearLongPressTimer();
  },

  methods: {
    handleCloseAllTooltips() {
      this.showTooltip = false;
    },

    handleOtherTooltipOpened(event) {
      // Close this tooltip if another one was opened and it's not this item
      if (event.detail && event.detail.instanceId !== this.item.instanceId) {
        this.showTooltip = false;
      }
    },

    handleImageError() {
      this.imageError = true;
    },

    handleMouseDown(event) {
      if (event.button === 2) {
        event.preventDefault();
        this.showItemTooltip(event);
      }
    },

    handleGlobalClick(event) {
      if (this.showTooltip && !this.$el.contains(event.target)) {
        this.showTooltip = false;
      }
    },

    handleGlobalTouch(event) {
      if (this.showTooltip && !this.$el.contains(event.target)) {
        this.showTooltip = false;
      }
    },

    handleTouchStart(event) {
      if (!this.item) return;

      const touch = event.touches[0];
      this.touchStartX = touch.clientX;
      this.touchStartY = touch.clientY;
      this.touchCurrentX = touch.clientX;
      this.touchCurrentY = touch.clientY;
      this.touchMoved = false;

      this.clearLongPressTimer();
      this.longPressTimer = setTimeout(() => {
        if (!this.touchMoved && !this.isDragging) {
          this.showItemTooltip(event);
        }
      }, this.longPressDuration);
    },

    handleTouchMove(event) {
      const touch = event.touches[0];

      const deltaX = Math.abs(touch.clientX - this.touchStartX);
      const deltaY = Math.abs(touch.clientY - this.touchStartY);

      if (deltaX > this.dragThreshold || deltaY > this.dragThreshold) {
        this.touchMoved = true;
        this.clearLongPressTimer();

        if (!this.isDragStarted) {
          this.isDragStarted = true;
          this.isDragging = true;
          this.onTouchDragStart(event);
        }

        this.onTouchDragMove(event);
      }
    },

    handleTouchEnd(event) {
      this.clearLongPressTimer();

      if (this.isDragging) {
        this.onTouchDragEnd(event);
      }

      this.isDragStarted = false;
    },

    clearLongPressTimer() {
      if (this.longPressTimer) {
        clearTimeout(this.longPressTimer);
        this.longPressTimer = null;
      }
    },

    onDragStart(event) {
      if (!this.item) return;

      this.isDragging = true;

      const rect = event.currentTarget.getBoundingClientRect();
      const clickOffsetX = event.clientX - rect.left;
      const clickOffsetY = event.clientY - rect.top;

      const cellX = Math.floor(clickOffsetX / this.cellSize);
      const cellY = Math.floor(clickOffsetY / this.cellSize);

      const clickedCell = {x: cellX, y: cellY};

      this.$emit('drag-start', {
        item: this.item,
        position: this.position,
        clickedCell: clickedCell
      });

      const dragData = {
        instanceId: this.item.instanceId,
        itemId: this.item.itemId,
        width: this.item.width,
        height: this.item.height,
        clickedCell: clickedCell
      };

      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('application/json', JSON.stringify(dragData));

      const dragPreview = document.createElement('div');
      dragPreview.className = 'drag-preview';

      const width = (this.item.width || 1) * this.cellSize;
      const height = (this.item.height || 1) * this.cellSize;

      dragPreview.style.width = `${width}px`;
      dragPreview.style.height = `${height}px`;
      dragPreview.style.backgroundColor = 'white';
      dragPreview.style.border = '2px dashed #444';
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
          cellX * this.cellSize + this.cellSize / 2,
          cellY * this.cellSize + this.cellSize / 2
      );

      setTimeout(() => {
        document.body.removeChild(dragPreview);
      }, 0);
    },

    onDragEnd(event) {
      this.isDragging = false;

      const gridElement = document.querySelector('.inventory-grid');
      if (!gridElement) return;

      const rect = gridElement.getBoundingClientRect();

      if (
          event.clientX < rect.left ||
          event.clientX > rect.right ||
          event.clientY < rect.top ||
          event.clientY > rect.bottom
      ) {
        this.$emit('item-deleted', {
          instanceId: this.item.instanceId,
          itemId: this.item.itemId
        });
      }
    },

    onTouchDragStart(event) {
      const touch = event.touches[0];
      this.touchCurrentX = touch.clientX;
      this.touchCurrentY = touch.clientY;

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
      ghost.style.backgroundColor = 'white';
      ghost.style.border = '2px dashed #444';
      ghost.style.borderRadius = '4px';
      ghost.style.position = 'fixed';
      ghost.style.zIndex = '9999';
      ghost.style.pointerEvents = 'none';
      ghost.style.opacity = '0.8';
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

    onTouchDragMove(event) {
      if (!this.isDragging) return;

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

    onTouchDragEnd(event) {
      const deltaX = this.touchCurrentX - this.touchStartX;
      const deltaY = this.touchCurrentY - this.touchStartY;

      this.removeTouchGhost();
      this.isDragging = false;

      const gridElement = document.querySelector('.inventory-grid');
      if (gridElement) {
        const rect = gridElement.getBoundingClientRect();

        if (
            this.touchCurrentX < rect.left ||
            this.touchCurrentX > rect.right ||
            this.touchCurrentY < rect.top ||
            this.touchCurrentY > rect.bottom
        ) {
          this.$emit('item-deleted', {
            instanceId: this.item.instanceId,
            itemId: this.item.itemId
          });
          return;
        }
      }

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

    showItemTooltip(event) {
      this.clearLongPressTimer();

      // First, dispatch an event to close all other tooltips
      document.dispatchEvent(new CustomEvent('close-all-tooltips'));

      // Then calculate position for this tooltip
      const rect = this.$el.getBoundingClientRect();
      const itemCenterX = rect.left + rect.width / 2;

      const viewportWidth = window.innerWidth;
      const viewportHeight = window.innerHeight;

      // Try positioning to the right first
      let posX = rect.right + 10;
      let posY = rect.top;

      // If not enough space on the right, try left
      if (posX + 200 > viewportWidth) {
        posX = rect.left - 210;
      }

      // If not enough space on left either, position below
      if (posX < 0) {
        posX = itemCenterX - 100;
        posY = rect.bottom + 10;
      }

      // If tooltip would be below viewport, position above
      if (posY + 150 > viewportHeight) {
        posY = rect.top - 160;
      }

      this.tooltipPosition = {x: posX, y: posY};
      this.showTooltip = true;

      // Dispatch event that this tooltip is now open
      document.dispatchEvent(new CustomEvent('open-tooltip', {
        detail: {
          instanceId: this.item.instanceId
        }
      }));
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
  overflow: visible;
  cursor: grab;
  z-index: 1;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background-color: white;
  border: 2px solid #ddd;
}

.inventory-item.is-dragging {
  opacity: 0.6;
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
</style>