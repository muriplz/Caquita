<script setup>
import SyncStore from "@/js/sync/SyncStore.js";
import CurrencyPanelItem from "@/components/ui/auth/CurrencyPanelItem.vue";
import {onMounted, ref} from "vue";

const beans = ref(0);
const rolls = ref(0);

onMounted(async () => {
  beans.value = await SyncStore.getBeans();
  rolls.value = await SyncStore.getRolls();
})
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
  background: #bbabab;
  border: 3px solid #333;
  padding: 4px 8px;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>