<template>
  <motion-v
      class="absolute w-6 h-6 bg-blue-500 border-2 border-white rounded-full cursor-grab z-[1000] shadow-lg select-none"
      :class="{ 'cursor-grabbing': isDragging }"
      :style="{ left: x + 'px', top: y + 'px', transform: 'translate(-50%, -50%)' }"
      :animate="{ scale: isDragging ? 0.95 : 1 }"
      :transition="{ duration: 0.2 }"
      @mousedown="startDrag"
      @touchstart="startDrag"
  />
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { motion } from 'motion-v'

const props = defineProps({
  x: Number,
  y: Number,
  isDragging: Boolean
})

const emit = defineEmits(['drag-start', 'drag-move', 'drag-end'])

const startDrag = (e) => {
  e.preventDefault()
  e.stopPropagation()

  emit('drag-start')

  const moveHandler = (e) => {
    e.preventDefault()
    e.stopPropagation()

    const parentRect = e.target.closest('.relative').getBoundingClientRect()
    const clientX = e.clientX || (e.touches && e.touches[0]?.clientX)
    const clientY = e.clientY || (e.touches && e.touches[0]?.clientY)

    const x = clientX - parentRect.left
    const y = clientY - parentRect.top

    emit('drag-move', { x, y })
  }

  const stopHandler = (e) => {
    e.preventDefault()
    e.stopPropagation()

    emit('drag-end')

    document.removeEventListener('mousemove', moveHandler)
    document.removeEventListener('mouseup', stopHandler)
    document.removeEventListener('touchmove', moveHandler)
    document.removeEventListener('touchend', stopHandler)
  }

  document.addEventListener('mousemove', moveHandler)
  document.addEventListener('mouseup', stopHandler)
  document.addEventListener('touchmove', moveHandler, { passive: false })
  document.addEventListener('touchend', stopHandler)
}
</script>