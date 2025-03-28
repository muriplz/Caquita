<script setup>
import SyncStore from "@/js/sync/SyncStore.js";
import CurrencyPanelItem from "@/components/ui/auth/CurrencyPanelItem.vue";

const beans = SyncStore.getBeans();
const rolls = SyncStore.getRolls();
</script>

<template>
  <div class="currency-panel">
    <CurrencyPanelItem :value="beans" name="beans"/>
    <CurrencyPanelItem :value="rolls" name="rolls"/>
  </div>
</template>

<style scoped>
.currency-panel {
  display: flex;
  gap: 8px;
  background: #5e4f42;
  border-radius: 8px;
  padding: 4px 8px;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>