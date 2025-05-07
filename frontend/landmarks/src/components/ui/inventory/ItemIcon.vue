<template>
  <div class="item-icon-container">
    <motion.div
        :animate="{ y: ['-4px', '-8px', '-4px'] }"
        :transition="{
        duration: 4,
        repeat: Infinity,
        ease: 'easeInOut'
      }"
    >
      <img
          :src="image"
          :alt="altText"
          class="item-image"
          @load="adjustImageSize"
          ref="itemImage"
      />
    </motion.div>
    <motion.div
        class="item-shadow"
        :animate="{
        width: ['80%', '70%', '80%'],
        filter: ['blur(2px)', 'blur(3px)', 'blur(2px)']
      }"
        :transition="{
        duration: 4,
        repeat: Infinity,
        ease: 'easeInOut'
      }"
    ></motion.div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { motion } from 'motion-v'
import Store from "@/js/Store.js"

const props = defineProps({
  itemId: {
    type: String,
    required: true
  },
  size: {
    type: Number,
    default: 64
  },
  altText: {
    type: String,
    default: 'Item icon'
  }
})

const itemImage = ref(null)
const item = computed(() => Store.getItemById(props.itemId))

const image = computed(() => {
  return `/images/items/${props.itemId.split(':').join('/')}.png`
})

const adjustImageSize = () => {
  if (!itemImage.value) return

  const img = itemImage.value
  const size = props.size + 'px'

  if (img.naturalWidth >= img.naturalHeight) {
    img.style.width = size
    img.style.height = 'auto'
  } else {
    img.style.width = 'auto'
    img.style.height = size
  }
}
</script>

<style scoped>
.item-icon-container {
  position: relative;
  display: inline-block;
}

.item-image {
  image-rendering: pixelated;
  position: relative;
  z-index: 2;
}

.item-shadow {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  height: 10px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 50%;
  z-index: 1;
}
</style>