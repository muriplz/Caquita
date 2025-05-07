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
  <div class="flex gap-2 modal px-2 py-1">
    <CurrencyPanelItem :value="beans" name="beans"/>
    <CurrencyPanelItem :value="rolls" name="rolls"/>
  </div>
</template>

<style scoped>

</style>