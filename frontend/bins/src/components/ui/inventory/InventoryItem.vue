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
const GAP_SIZE = 4

watch(() => props.inventoryItem.cells, (newCells) => {
  cells.value = newCells
}, {deep: true})

const shape = computed(() => item.value.shape || [[1]])

const getAnchorCol = computed(() => {
  let minCol = Infinity
  for (const cell of cells.value) {
    minCol = Math.min(minCol, cell.col)
  }

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

const getAnchorRow = computed(() => {
  let minRow = Infinity
  for (const cell of cells.value) {
    minRow = Math.min(minRow, cell.row)
  }

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

const anchorPosition = computed(() => ({
  col: getAnchorCol.value,
  row: getAnchorRow.value
}))

const position = computed(() => {
  let minRow = Infinity
  let minCol = Infinity

  cells.value.forEach(cell => {
    minRow = Math.min(minRow, cell.row)
    minCol = Math.min(minCol, cell.col)
  })

  return {
    x: minCol * 64 + minCol * GAP_SIZE,
    y: minRow * 64 + minRow * GAP_SIZE
  }
})

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

  return {
    width: width * 64 + (width - 1) * GAP_SIZE,
    height: height * 64 + (height - 1) * GAP_SIZE
  }
})

const image = `/images/items/${item.value.id.split(':').join('/')}.png`

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

const animatedPosition = reactive({
  x: position.value.x,
  y: position.value.y
})

const originalPosition = reactive({
  x: 0,
  y: 0
})

watch(position, (newPos) => {
  if (!isDragging.value) {
    animatedPosition.x = newPos.x
    animatedPosition.y = newPos.y
  }
})

function handleCellInteraction(event) {
  event.target.dataset.validCellClick = 'true';
}

function handleDragStart(event, info) {
  const validCellClick = event.target.closest('.hit-areas-wrapper')?.querySelector('[data-valid-cell-click="true"]');

  if (validCellClick) {
    validCellClick.dataset.validCellClick = '';

    // Get coordinates from either mouse or touch event
    const clickX = info.point.x;
    const clickY = info.point.y;

    isDragging.value = true;

    originalPosition.x = animatedPosition.x;
    originalPosition.y = animatedPosition.y;

    dragStartOffset.value = {
      x: clickX - animatedPosition.x,
      y: clickY - animatedPosition.y
    };
  } else {
    isDragging.value = false;
    return false;
  }
}

function pixelToGridPosition(pixelX, pixelY) {
  const col = Math.round(pixelX / (64 + GAP_SIZE))
  const row = Math.round(pixelY / (64 + GAP_SIZE))
  return { col, row }
}

function gridToPixelPosition(col, row) {
  return {
    x: col * 64 + col * GAP_SIZE,
    y: row * 64 + row * GAP_SIZE
  }
}

function calculateNewCells(oldCells, deltaCol, deltaRow) {
  return oldCells.map(cell => ({
    col: cell.col + deltaCol,
    row: cell.row + deltaRow
  }));
}

async function handleDragEnd(event, info) {
  try {
    if (!isDragging.value) {
      return
    }

    const currentX = info.point.x - dragStartOffset.value.x
    const currentY = info.point.y - dragStartOffset.value.y

    const topLeftPos = pixelToGridPosition(currentX, currentY)

    const newAnchorCol = topLeftPos.col - (getMinCol() - anchorPosition.value.col)
    const newAnchorRow = topLeftPos.row - (getMinRow() - anchorPosition.value.row)

    console.log("Original anchor:", anchorPosition.value)
    console.log("New grid position:", topLeftPos)
    console.log("New anchor position:", { col: newAnchorCol, row: newAnchorRow })

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
    console.log("Move result:", result)

    if (result) {
      cells.value = newCells
      Store.updateItemPosition(props.inventoryItem, newCells)
      originalPosition.x = snappedPixelPos.x
      originalPosition.y = snappedPixelPos.y
    } else {
      console.warn("Server rejected move, reverting")
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
          @mousedown="handleCellInteraction"
          @touchstart="handleCellInteraction"
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
  pointer-events: none;
}

.cell-hit-area {
  cursor: grab;
  background-color: rgba(255, 255, 255, 0.01);
  z-index: 2;
  pointer-events: all;
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