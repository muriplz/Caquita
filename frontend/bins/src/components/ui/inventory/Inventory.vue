<template>
  <div class="inventory-container">
    <InventoryGrid />
  </div>
</template>

<script setup>

import InventoryGrid from "@/components/ui/inventory/InventoryGrid.vue";
</script>

<style scoped>
.inventory-container {
  padding: 8px;
  background-color: #373737;
  border-radius: 4px;
  max-width: fit-content;
}
</style>