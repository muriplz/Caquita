<script setup>
import {onMounted, ref} from "vue";
import StatsApi from "@/js/stats/StatsApi.js";

const stats = ref(null)

onMounted(async () => {
  stats.value = await StatsApi.getStats()
})
</script>

<template>
  <section class="grid grid-cols-2 grid-rows-2 gap-18 mt-12">
    <div class="text-center justify-self-end pr-8">
      <h2>Users</h2>
      <h3>{{ stats ? stats.USERS : 0 }}</h3>
    </div>

    <div class="text-center justify-self-start pl-8">
      <h2>Views</h2>
      <h3>{{ stats ? stats.VIEWS : 0 }}</h3>
    </div>

    <div class="text-center col-span-2">
      <h2>Other</h2>
      <h3>whatever</h3>
    </div>
  </section>
</template>

<style scoped>

</style>