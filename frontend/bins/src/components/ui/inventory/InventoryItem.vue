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

// Handle drag start
function handleDragStart(event, info) {
  isDragging.value = true

  // Store the original position for returning on failed drops
  originalPosition.x = position.value.x
  originalPosition.y = position.value.y

  // Calculate offset from anchor to click position
  const clickX = info.point.x
  const clickY = info.point.y

  // Store offset from click to top-left corner of item
  dragStartOffset.value = {
    x: clickX - position.value.x,
    y: clickY - position.value.y
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
    // Convert drag position to grid coordinates by accounting for drag offset
    const topLeftX = info.point.x - dragStartOffset.value.x
    const topLeftY = info.point.y - dragStartOffset.value.y

    // Calculate which grid cell we're over, accounting for gaps
    const { col: topLeftCol, row: topLeftRow } = pixelToGridPosition(topLeftX, topLeftY)

    // Calculate the original position in grid coordinates
    const { col: origCol, row: origRow } = pixelToGridPosition(position.value.x, position.value.y)

    // Calculate the movement from original position
    const deltaCol = topLeftCol - origCol
    const deltaRow = topLeftRow - origRow

    // Skip if no movement
    if (deltaCol === 0 && deltaRow === 0) {
      returnToOriginalPosition()
      return
    }

    // Calculate the new position for the anchor based on the movement
    const newAnchorCol = anchorPosition.value.col + deltaCol
    const newAnchorRow = anchorPosition.value.row + deltaRow

    console.log("Original anchor:", anchorPosition.value)
    console.log("Delta movement:", { col: deltaCol, row: deltaRow })
    console.log("New anchor position:", { col: newAnchorCol, row: newAnchorRow })

    // Check if placement is valid using the raw inventory coordinates
    const canPlace = await InventoryApi.canPlace(props.inventoryItem, newAnchorCol, newAnchorRow)
    console.log("Can place:", canPlace)

    if (canPlace) {
      // Calculate the new cells before making the API call
      const newCells = calculateNewCells(cells.value, deltaCol, deltaRow)

      // Make a clone of the item with new cells for client-side update
      const updatedItem = {
        ...props.inventoryItem,
        cells: newCells
      }

      // Get new pixel position accounting for grid gaps
      const newPixelPos = gridToPixelPosition(topLeftCol, topLeftRow)

      // Update animated position to the new position
      animatedPosition.x = newPixelPos.x
      animatedPosition.y = newPixelPos.y

      // Update the item in the store immediately for responsive UI
      Store.updateItemPosition(props.inventoryItem, newCells)

      // Update local cells reference
      cells.value = newCells

      // Move item using the raw inventory coordinates (server update)
      const result = await InventoryApi.move(props.inventoryItem, newAnchorCol, newAnchorRow)
      console.log("Move result:", result)

      // If server update failed, revert changes
      if (!result) {
        console.warn("Server update failed, reverting changes")
        Store.updateItemPosition(updatedItem, props.inventoryItem.cells)
        cells.value = props.inventoryItem.cells
        returnToOriginalPosition()
      }
    } else {
      // Return to original position if can't place
      returnToOriginalPosition()
    }
  } catch (error) {
    console.error('Error moving item:', error)
    // Return to original position on error
    returnToOriginalPosition()
  } finally {
    isDragging.value = false
  }
}

// Helper to return item to original position
function returnToOriginalPosition() {
  animatedPosition.x = originalPosition.x
  animatedPosition.y = originalPosition.y
  isDragging.value = false
}

// Initialize animated position
onMounted(() => {
  animatedPosition.x = position.value.x
  animatedPosition.y = position.value.y
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
    <img
        :src="image"
        :alt="item.id"
        :style="{
        width: `${size.width}px`,
        height: `${size.height}px`,
        objectFit: 'contain'
      }"
    />
  </motion.div>
</template>

<style scoped>
.inventory-item {
  cursor: grab;
  user-select: none;
}

.inventory-item:active {
  cursor: grabbing;
}

.inventory-item img {
  image-rendering: pixelated;
  pointer-events: none;
}
</style>