<template>
  <motion.div
      class="inventory-item"
      :style="{
      width: `${size.width}px`,
      height: `${size.height}px`,
      position: 'absolute',
      zIndex: isDragging ? 20 : 10,
      willChange: 'transform',
      transform: isInitialRender ?
        `translate3d(${animatedPosition.x}px, ${animatedPosition.y}px, 0)` : undefined
    }"
      :animate="{
      x: isDragging ? undefined : animatedPosition.x,
      y: isDragging ? undefined : animatedPosition.y,
      transition: getTransition
    }"
      drag
      :drag-constraints="inventoryConstraints"
      :drag-elastic="0.1"
      :drag-momentum="false"
      :dragTransition="{
      power: 0.05,
      timeConstant: 200
    }"
      @dragStart="handleDragStart"
      @dragEnd="handleDragEnd"
  >
    <div class="hit-areas-wrapper">
      <div
          v-for="cell in cells"
          :key="`${cell.row}-${cell.col}`"
          class="cell-hit-area"
          :style="{
          left: `${(cell.col - getMinCol) * (64 + GAP_SIZE)}px`,
          top: `${(cell.row - getMinRow) * (64 + GAP_SIZE)}px`
        }"
          @mousedown.passive="handleCellInteraction"
          @touchstart.passive="handleCellInteraction"
          @mousedown="startPressTimer"
          @mouseup="clearPressTimer"
          @mouseleave="clearPressTimer"
          @touchstart="startPressTimer"
          @touchend="clearPressTimer"
          @touchcancel="clearPressTimer"
      ></div>
    </div>

    <!-- New rotation container with continuous rotation -->
    <div class="item-wrapper">
      <div
          ref="rotationEl"
          class="item-visual"
          :style="{
          transform: `rotate(${currentRotationDegrees}deg)`
        }"
      >
        <img
            :src="image"
            :alt="item?.id || ''"
            class="item-image"
            :style="{
              width: isRotated ? `${size.height}px` : `${size.width}px`,
              height: isRotated ? `${size.width}px` : `${size.height}px`,
              maxWidth: 'none',
              maxHeight: 'none',
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: `translate(-50%, -50%)`
            }"
        />
      </div>
    </div>
  </motion.div>
</template>

<script setup>
import {motion} from 'motion-v'
import Store from "@/js/Store.js"
import {ref, computed, reactive, onMounted, watch, shallowRef, nextTick} from "vue"
import InventoryApi from "@/components/ui/inventory/js/InventoryApi.js"
import {getIpAddress} from "@/js/static.js";

const props = defineProps({
  inventoryItem: {
    type: Object,
    required: true
  }
})

// Core state
const item = shallowRef(Store.getItemById(props.inventoryItem.id))
const cells = ref(props.inventoryItem.cells || [])
const orientation = ref(props.inventoryItem.orientation || 'UP')
const isDragging = ref(false)
const isInitialRender = ref(true)
const dragStartOffset = ref({x: 0, y: 0})
const GAP_SIZE = 4

// Rotation state
const pressTimer = ref(null)
const longPressDuration = 800
const rotationEl = ref(null)
const lastRotationDirection = ref(true) // true = clockwise
const currentRotationDegrees = ref(0)
const rotationCount = ref(0)

// Track if the item is rotated sideways (for image sizing)
const isRotated = computed(() => {
  return orientation.value === 'LEFT' || orientation.value === 'RIGHT'
})

// Watch for changes
watch(() => props.inventoryItem.cells, (newCells) => {
  if (newCells) cells.value = newCells
})

// Watch orientation changes and apply continuous rotation
watch(() => props.inventoryItem.orientation, (newOrientation, oldOrientation) => {
  if (newOrientation) {
    orientation.value = newOrientation

    // Apply continuous rotation
    applyRotation(oldOrientation, newOrientation)
  }
})

// Apply rotation with continuous direction
function applyRotation(oldOrientation, newOrientation) {
  if (!oldOrientation || oldOrientation === newOrientation) {
    // Initial setting or no change
    currentRotationDegrees.value = getOrientationDegrees(newOrientation)
    return
  }

  const oldDegrees = getOrientationDegrees(oldOrientation)
  const newDegrees = getOrientationDegrees(newOrientation)

  // Determine if we're going clockwise or counterclockwise
  // This logic checks if we're rotating from RIGHT (270) to UP (0)
  // or from LEFT (90) to DOWN (180), etc.
  if ((oldDegrees === 270 && newDegrees === 0) ||
      (oldDegrees === 0 && newDegrees === 90) ||
      (oldDegrees === 90 && newDegrees === 180) ||
      (oldDegrees === 180 && newDegrees === 270)) {
    // Clockwise
    lastRotationDirection.value = true
  } else {
    // Counterclockwise
    lastRotationDirection.value = false
  }

  // For continuous rotation in the same direction
  if (lastRotationDirection.value) {
    // Clockwise - always add 90 degrees
    rotationCount.value++
    currentRotationDegrees.value = rotationCount.value * 90
  } else {
    // Counterclockwise - always subtract 90 degrees
    rotationCount.value--
    currentRotationDegrees.value = rotationCount.value * 90
  }
}

// Get rotation degrees based on orientation
function getOrientationDegrees(orientationValue) {
  const current = orientationValue || 'UP'
  switch(current) {
    case 'UP': return 0
    case 'RIGHT': return 270
    case 'DOWN': return 180
    case 'LEFT': return 90
    default: return 0
  }
}

// Calculate position and size based on cells
const positionAndSize = computed(() => {
  if (!cells.value || cells.value.length === 0) {
    return {
      position: { x: 0, y: 0 },
      size: { width: 64, height: 64 },
      minRow: 0,
      minCol: 0
    }
  }

  let minRow = Number.MAX_SAFE_INTEGER
  let minCol = Number.MAX_SAFE_INTEGER
  let maxRow = -1
  let maxCol = -1

  for (const cell of cells.value) {
    minRow = Math.min(minRow, cell.row)
    minCol = Math.min(minCol, cell.col)
    maxRow = Math.max(maxRow, cell.row)
    maxCol = Math.max(maxCol, cell.col)
  }

  // Handle edge case where min/max weren't updated
  if (minRow === Number.MAX_SAFE_INTEGER) minRow = 0
  if (minCol === Number.MAX_SAFE_INTEGER) minCol = 0
  if (maxRow === -1) maxRow = 0
  if (maxCol === -1) maxCol = 0

  const width = maxCol - minCol + 1
  const height = maxRow - minRow + 1

  return {
    position: {
      x: minCol * 64 + minCol * GAP_SIZE,
      y: minRow * 64 + minRow * GAP_SIZE
    },
    size: {
      width: width * 64 + (width - 1) * GAP_SIZE,
      height: height * 64 + (height - 1) * GAP_SIZE
    },
    minRow,
    minCol
  }
})

// Simplified helper computed properties
const position = computed(() => positionAndSize.value.position)
const size = computed(() => positionAndSize.value.size)
const getMinRow = computed(() => positionAndSize.value.minRow)
const getMinCol = computed(() => positionAndSize.value.minCol)
const image = computed(() => `/images/items/${item.value?.id.split(':').join('/')}.png`)

// Get anchor position for movement calculations
const anchorPosition = computed(() => {
  if (!cells.value || cells.value.length === 0) {
    return { col: 0, row: 0 }
  }

  let minRow = Number.MAX_SAFE_INTEGER
  let minCol = Number.MAX_SAFE_INTEGER

  for (const cell of cells.value) {
    minRow = Math.min(minRow, cell.row)
    minCol = Math.min(minCol, cell.col)
  }

  // Handle edge case
  if (minRow === Number.MAX_SAFE_INTEGER) minRow = 0
  if (minCol === Number.MAX_SAFE_INTEGER) minCol = 0

  return { col: minCol, row: minRow }
})

// Inventory constraints for dragging
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

// Position tracking
const animatedPosition = reactive({
  x: 0,
  y: 0
})

const originalPosition = reactive({
  x: 0,
  y: 0
})

// Update animated position when actual position changes
watch(position, (newPos) => {
  if (!isDragging.value) {
    animatedPosition.x = newPos.x
    animatedPosition.y = newPos.y
  }
}, { immediate: true })

// Get transition for animations
const getTransition = computed(() => {
  if (isInitialRender.value) {
    return {duration: 0}
  }

  return {
    type: 'spring',
    stiffness: 300,
    damping: 25
  }
})

function handleCellInteraction(event) {
  event.target.dataset.validCellClick = 'true'
}

// Long press for rotation
function startPressTimer(event) {
  if (isDragging.value) return

  clearTimeout(pressTimer.value)

  pressTimer.value = setTimeout(() => {
    if (!isDragging.value) {
      rotateItem(event)
    }
  }, longPressDuration)
}

function clearPressTimer() {
  clearTimeout(pressTimer.value)
}

// Handle rotation
async function rotateItem(event) {
  try {
    // Find the cell that was clicked
    const cell = event.target.closest('.cell-hit-area')
    if (!cell) return

    // Get the cell's position
    const style = getComputedStyle(cell)
    const leftPx = parseInt(style.left) || 0
    const topPx = parseInt(style.top) || 0

    const colOffset = Math.round(leftPx / (64 + GAP_SIZE))
    const rowOffset = Math.round(topPx / (64 + GAP_SIZE))

    const heldCol = getMinCol.value + colOffset
    const heldRow = getMinRow.value + rowOffset

    // Store current orientation for rotation animation
    const currentOrient = orientation.value || 'UP'

    // Set rotation direction to clockwise (matches our visual expectations)
    lastRotationDirection.value = true

    // Prepare data for API
    const itemClone = {
      id: props.inventoryItem.id,
      cells: cells.value,
      orientation: currentOrient,
      nbt: props.inventoryItem.nbt || '{}'
    }

    // Call API - always send false to get clockwise rotation
    const result = await InventoryApi.rotate(itemClone, false, heldCol, heldRow)

    if (result) {
      // Update on success
      orientation.value = result.orientation
      cells.value = result.cells
      Store.updateItemOrientation(props.inventoryItem, result.orientation, result.cells)
      await Store.updateInventory()
    }
  } catch (error) {
    console.error('Error rotating item:', error)
  }
}

// Handle drag start
function handleDragStart(event, info) {
  clearPressTimer()

  const validCellClick = event.target.closest('.hit-areas-wrapper')?.querySelector('[data-valid-cell-click="true"]')

  if (validCellClick) {
    validCellClick.dataset.validCellClick = ''

    const clickX = info.point.x
    const clickY = info.point.y

    isDragging.value = true

    originalPosition.x = animatedPosition.x
    originalPosition.y = animatedPosition.y

    dragStartOffset.value = {
      x: clickX - animatedPosition.x,
      y: clickY - animatedPosition.y
    }
  } else {
    isDragging.value = false
    return false
  }
}

// Convert pixel position to grid position
function pixelToGridPosition(pixelX, pixelY) {
  return {
    col: Math.round(pixelX / (64 + GAP_SIZE)),
    row: Math.round(pixelY / (64 + GAP_SIZE))
  }
}

// Convert grid position to pixel position
function gridToPixelPosition(col, row) {
  return {
    x: col * 64 + col * GAP_SIZE,
    y: row * 64 + row * GAP_SIZE
  }
}

// Calculate new cells based on movement
function calculateNewCells(oldCells, deltaCol, deltaRow) {
  return oldCells.map(cell => ({
    col: cell.col + deltaCol,
    row: cell.row + deltaRow
  }))
}

// Handle drag end
async function handleDragEnd(event, info) {
  try {
    if (!isDragging.value) {
      return
    }

    const currentX = info.point.x - dragStartOffset.value.x
    const currentY = info.point.y - dragStartOffset.value.y

    const topLeftPos = pixelToGridPosition(currentX, currentY)

    const newAnchorCol = topLeftPos.col - (getMinCol.value - anchorPosition.value.col)
    const newAnchorRow = topLeftPos.row - (getMinRow.value - anchorPosition.value.row)

    const deltaCol = newAnchorCol - anchorPosition.value.col
    const deltaRow = newAnchorRow - anchorPosition.value.row

    if (deltaCol === 0 && deltaRow === 0) {
      animatedPosition.x = originalPosition.x
      animatedPosition.y = originalPosition.y
      isDragging.value = false
      return
    }

    const newCells = calculateNewCells(cells.value, deltaCol, deltaRow)

    const snappedPixelPos = gridToPixelPosition(topLeftPos.col, topLeftPos.row)
    animatedPosition.x = snappedPixelPos.x
    animatedPosition.y = snappedPixelPos.y

    const result = await InventoryApi.move(props.inventoryItem, newAnchorCol, newAnchorRow)

    if (result) {
      cells.value = result.cells || newCells
      Store.updateItemPosition(props.inventoryItem, cells.value)
      originalPosition.x = snappedPixelPos.x
      originalPosition.y = snappedPixelPos.y
    } else {
      animatedPosition.x = originalPosition.x
      animatedPosition.y = originalPosition.y
    }
  } catch (error) {
    console.error('Error moving item:', error)
    animatedPosition.x = originalPosition.x
    animatedPosition.y = originalPosition.y
  } finally {
    isDragging.value = false
  }
}

// Initialize on mount
onMounted(() => {
  // Set initial position
  animatedPosition.x = position.value.x
  animatedPosition.y = position.value.y
  originalPosition.x = position.value.x
  originalPosition.y = position.value.y

  // Set initial rotation degrees
  currentRotationDegrees.value = getOrientationDegrees(orientation.value)

  // Initialize rotation count based on current orientation
  if (orientation.value === 'RIGHT') rotationCount.value = 3
  else if (orientation.value === 'DOWN') rotationCount.value = 2
  else if (orientation.value === 'LEFT') rotationCount.value = 1
  else rotationCount.value = 0

  // Set initial render to false after a short delay
  setTimeout(() => {
    isInitialRender.value = false
  }, 50)
})
</script>

<style scoped>
.inventory-item {
  cursor: default;
  user-select: none;
  pointer-events: none;
  transform: translateZ(0);
}

.hit-areas-wrapper {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 5;
}

.cell-hit-area {
  cursor: grab;
  background-color: rgba(255, 255, 255, 0.01);
  z-index: 6;
  pointer-events: all;
  position: absolute;
  width: 64px;
  height: 64px;
}

.cell-hit-area:active {
  cursor: grabbing;
}

/* Improved styling for rotation */
.item-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-visual {
  width: 100%;
  height: 100%;
  position: relative;
  transform-origin: center;
  transition: transform 0.4s cubic-bezier(0.2, 0.85, 0.4, 1);
  will-change: transform;
  overflow: visible;
}

.item-image {
  image-rendering: pixelated;
  pointer-events: none;
  transition: width 0.4s, height 0.4s;
  transform-origin: center;
}
</style>