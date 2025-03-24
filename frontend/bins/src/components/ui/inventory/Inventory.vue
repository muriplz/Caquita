<template>
  <div class="inventory-page">
    <InventoryGrid />
  </div>
</template>

<script setup>
import InventoryGrid from "./InventoryGrid.vue"
import {onBeforeMount, onMounted} from "vue"
import Store from "@/js/Store.js"

onBeforeMount(() => {
  Store.updateInventory()
})
</script>

<style scoped>
.inventory-page {
  padding: 20px;
  max-width: fit-content;
}
</style>