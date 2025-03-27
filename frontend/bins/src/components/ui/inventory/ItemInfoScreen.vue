<template>
  <div class="item-info-screen">
    <ItemInfo />
  </div>
</template>

<script setup>
import ItemInfo from './ItemInfo.vue'

defineEmits(['close'])
</script>

<style scoped>
.item-info-screen {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}
</style>