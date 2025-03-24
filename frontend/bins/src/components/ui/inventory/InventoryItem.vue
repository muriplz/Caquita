<script setup>
import {motion} from 'motion-v'
import Store from "@/js/Store.js"
import {ref, computed, reactive, onMounted, watch} from "vue"
import InventoryApi from "@/components/ui/inventory/js/InventoryApi.js"

const props = defineProps({
  inventoryItem: {
    type: Object,
    required: true
  }
})

const item = ref(Store.getItemById(props.inventoryItem.id))
const cells = ref(props.inventoryItem.cells)
const isDragging = ref(false)
const dragStartOffset = ref({x: 0, y: 0})
const GAP_SIZE = 4 // Grid gap size in pixels

// Watch for changes in props
watch(() => props.inventoryItem.cells, (newCells) => {
  cells.value = newCells
}, {deep: true})

// Get the shape from the item
const shape = computed(() => item.value.shape || [[1]])

// Calculate anchor column - matching backend logic exactly
const getAnchorCol = computed(() => {
  // Find minimum column in cells
  let minCol = Infinity
  for (const cell of cells.value) {
    minCol = Math.min(minCol, cell.col)
  }

  // Find first 1 in shape by column
  let shapeOffsetX = -1
  outerLoop: for (let x = 0; x < shape.value[0].length; x++) {
    for (let y = 0; y < shape.value.length; y++) {
      if (shape.value[y][x] === 1) {
        shapeOffsetX = x
        break outerLoop
      }
    }
  }

  return minCol - shapeOffsetX
})

// Calculate anchor row - matching backend logic exactly
const getAnchorRow = computed(() => {
  // Find minimum row in cells
  let minRow = Infinity
  for (const cell of cells.value) {
    minRow = Math.min(minRow, cell.row)
  }

  // Find first 1 in shape by row
  let shapeOffsetY = -1
  outerLoop: for (let y = 0; y < shape.value.length; y++) {
    for (let x = 0; x < shape.value[y].length; x++) {
      if (shape.value[y][x] === 1) {
        shapeOffsetY = y
        break outerLoop
      }
    }
  }

  return minRow - shapeOffsetY
})

// Anchor position for API calls
const anchorPosition = computed(() => ({
  col: getAnchorCol.value,
  row: getAnchorRow.value
}))

// Visual position for rendering (top-left corner)
const position = computed(() => {
  let minRow = Infinity
  let minCol = Infinity

  cells.value.forEach(cell => {
    minRow = Math.min(minRow, cell.row)
    minCol = Math.min(minCol, cell.col)
  })

  // Include grid gap in position calculation
  return {
    x: minCol * 64 + minCol * GAP_SIZE,
    y: minRow * 64 + minRow * GAP_SIZE
  }
})

// Calculate item size including gaps
const size = computed(() => {
  let minRow = Infinity
  let minCol = Infinity
  let maxRow = -Infinity
  let maxCol = -Infinity

  cells.value.forEach(cell => {
    minRow = Math.min(minRow, cell.row)
    minCol = Math.min(minCol, cell.col)
    maxRow = Math.max(maxRow, cell.row)
    maxCol = Math.max(maxCol, cell.col)
  })

  const width = maxCol - minCol + 1
  const height = maxRow - minRow + 1

  // Width and height including the cell size and gaps
  return {
    width: width * 64 + (width - 1) * GAP_SIZE,
    height: height * 64 + (height - 1) * GAP_SIZE
  }
})

const image = `/images/items/${item.value.id.split(':').join('/')}.png`

// Get inventory dimensions for constraints
const inventory = computed(() => Store.getInventory())
const inventoryConstraints = computed(() => {
  const width = inventory.value?.width || 8
  const height = inventory.value?.height || 8

  return {
    top: 0,
    left: 0,
    right: width * 64 + (width - 1) * GAP_SIZE - size.value.width,
    bottom: height * 64 + (height - 1) * GAP_SIZE - size.value.height
  }
})

// Tracking for animation
const animatedPosition = reactive({
  x: position.value.x,
  y: position.value.y
})

// Store original position for returning on failed drops
const originalPosition = reactive({
  x: 0,
  y: 0
})

// Update animated position when not dragging
watch(position, (newPos) => {
  if (!isDragging.value) {
    animatedPosition.x = newPos.x
    animatedPosition.y = newPos.y
  }
})

function handleCellMouseDown(event) {
  // This marks that a valid cell was clicked
  // We'll check this in handleDragStart
  event.target.dataset.validCellClick = 'true';
}

// Modify handleDragStart
function handleDragStart(event, info) {
  // Check if the mousedown event came from a valid cell
  const validCellClick = event.target.closest('.hit-areas-wrapper')?.querySelector('[data-valid-cell-click="true"]');

  if (validCellClick) {
    // Reset the marker
    validCellClick.dataset.validCellClick = '';

    // Get the click position
    const clickX = info.point.x;
    const clickY = info.point.y;

    isDragging.value = true;

    // Store the original position
    originalPosition.x = animatedPosition.x;
    originalPosition.y = animatedPosition.y;

    // Store offset from click to top-left corner of item
    dragStartOffset.value = {
      x: clickX - animatedPosition.x,
      y: clickY - animatedPosition.y
    };
  } else {
    // Prevent the drag operation
    isDragging.value = false;
    return false;
  }
}

// Convert pixel position to grid position accounting for gaps
function pixelToGridPosition(pixelX, pixelY) {
  // The formula accounts for both cell size (64px) and gap (4px)
  const col = Math.round(pixelX / (64 + GAP_SIZE))
  const row = Math.round(pixelY / (64 + GAP_SIZE))
  return { col, row }
}

// Convert grid position to pixel position accounting for gaps
function gridToPixelPosition(col, row) {
  return {
    x: col * 64 + col * GAP_SIZE,
    y: row * 64 + row * GAP_SIZE
  }
}

// Calculates new cells based on anchor movement
function calculateNewCells(oldCells, deltaCol, deltaRow) {
  return oldCells.map(cell => ({
    col: cell.col + deltaCol,
    row: cell.row + deltaRow
  }));
}

// Handle drag end
async function handleDragEnd(event, info) {
  try {
    // Only process if we're actually dragging
    if (!isDragging.value) {
      return
    }

    // Get current dragged position (where user dropped the item)
    const currentX = info.point.x - dragStartOffset.value.x
    const currentY = info.point.y - dragStartOffset.value.y

    // Convert to grid coordinates using the top-left corner
    const topLeftPos = pixelToGridPosition(currentX, currentY)

    // Calculate the new anchor position directly
    // Instead of calculating delta movement first
    const newAnchorCol = topLeftPos.col - (getMinCol() - anchorPosition.value.col)
    const newAnchorRow = topLeftPos.row - (getMinRow() - anchorPosition.value.row)

    console.log("Original anchor:", anchorPosition.value)
    console.log("New grid position:", topLeftPos)
    console.log("New anchor position:", { col: newAnchorCol, row: newAnchorRow })

    // Calculate how many grid cells we moved
    const deltaCol = newAnchorCol - anchorPosition.value.col
    const deltaRow = newAnchorRow - anchorPosition.value.row

    // Skip if no movement
    if (deltaCol === 0 && deltaRow === 0) {
      // Snap back to grid position and exit
      animatedPosition.x = originalPosition.x
      animatedPosition.y = originalPosition.y
      isDragging.value = false
      return
    }

    // Calculate the new cells
    const newCells = calculateNewCells(cells.value, deltaCol, deltaRow)

    // Update visual position
    const snappedPixelPos = gridToPixelPosition(topLeftPos.col, topLeftPos.row)
    animatedPosition.x = snappedPixelPos.x
    animatedPosition.y = snappedPixelPos.y

    // Send move request to server
    const result = await InventoryApi.move(props.inventoryItem, newAnchorCol, newAnchorRow)
    console.log("Move result:", result)

    if (result) {
      // Update local cells reference on success
      cells.value = newCells

      // Update the item in the store
      Store.updateItemPosition(props.inventoryItem, newCells)

      // On successful move, update the originalPosition to the new position
      originalPosition.x = snappedPixelPos.x
      originalPosition.y = snappedPixelPos.y
    } else {
      // If server rejects the move, revert all changes
      console.warn("Server rejected move, reverting")

      // Revert to original position
      animatedPosition.x = originalPosition.x
      animatedPosition.y = originalPosition.y
    }
  } catch (error) {
    console.error('Error moving item:', error)
    // Return to original position on error
    animatedPosition.x = originalPosition.x
    animatedPosition.y = originalPosition.y
  } finally {
    isDragging.value = false
  }
}

function getMinRow() {
  let minRow = Infinity
  cells.value.forEach(cell => {
    minRow = Math.min(minRow, cell.row)
  })
  return minRow
}

function getMinCol() {
  let minCol = Infinity
  cells.value.forEach(cell => {
    minCol = Math.min(minCol, cell.col)
  })
  return minCol
}
// Initialize animated position
onMounted(() => {
  animatedPosition.x = position.value.x
  animatedPosition.y = position.value.y
  originalPosition.x = position.value.x
  originalPosition.y = position.value.y
})
</script>

<template>
  <motion.div
      class="inventory-item"
      :style="{
      width: `${size.width}px`,
      height: `${size.height}px`,
      position: 'absolute',
      zIndex: isDragging ? 20 : 10
    }"
      :animate="{
      x: isDragging ? undefined : animatedPosition.x,
      y: isDragging ? undefined : animatedPosition.y,
      transition: {
        type: 'spring',
        stiffness: 500,
        damping: 30
      }
    }"
      drag
      :drag-constraints="inventoryConstraints"
      :drag-elastic="0.2"
      :drag-momentum="false"
      :dragTransition="{
      power: 0.1,
      timeConstant: 250
    }"
      @dragStart="handleDragStart"
      @dragEnd="handleDragEnd"
  >
    <!-- Create a wrapper that contains just the cell hit areas -->
    <div class="hit-areas-wrapper" :style="{ width: '100%', height: '100%', position: 'absolute', top: 0, left: 0 }">
      <div
          v-for="cell in cells"
          :key="`${cell.row}-${cell.col}`"
          class="cell-hit-area"
          :style="{
          position: 'absolute',
          width: '64px',
          height: '64px',
          left: `${(cell.col - getMinCol()) * (64 + GAP_SIZE)}px`,
          top: `${(cell.row - getMinRow()) * (64 + GAP_SIZE)}px`
        }"
          @mousedown="handleCellMouseDown"
      ></div>
    </div>

    <img
        :src="image"
        :alt="item.id"
        :style="{
        width: `${size.width}px`,
        height: `${size.height}px`,
        objectFit: 'contain',
        pointerEvents: 'none'
      }"
    />
  </motion.div>
</template>

<style scoped>
.inventory-item {
  cursor: default;
  user-select: none;
}

.cell-hit-area {
  cursor: grab;
  background-color: rgba(255, 255, 255, 0.01); /* Nearly invisible but allows clicking */
  z-index: 2; /* Ensure hit areas are above the image */
}

.cell-hit-area:active {
  cursor: grabbing;
}

.inventory-item img {
  image-rendering: pixelated;
  pointer-events: none;
  position: absolute;
  top: 0;
  left: 0;
}
</style>