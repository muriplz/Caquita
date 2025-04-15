<script setup>

defineProps({
  value: Number,
  name: String
})
</script>

<template>
  <div class="item">
    <img :src="'/images/ui/currencies/' + name + '.png'" :alt="value" />
    <div class="currency-value">{{ value }}</div>
  </div>
</template>

<style scoped>
.item {
  display: flex;
  gap: 4px;
  align-items: center;
}

img {
  width: 24px;
  height: 24px;
  image-rendering: pixelated;
}
</style>