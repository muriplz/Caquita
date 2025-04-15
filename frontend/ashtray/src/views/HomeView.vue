<script setup>
import router from "@/router/index.js";
import StatsApi from "../js/stats/StatsApi.js";
import { onMounted, ref } from "vue";
import { version } from '/package.json'

const stats = ref(null)
const onlines = ref(0)

onMounted(async () => {
  stats.value = await StatsApi.getStats()
  onlines.value = await StatsApi.getOnlines()
})
</script>

<template>
  <div class="min-w-screen bg-recycle-green-500">
    <section class="relative min-h-screen">
      <img
          src="/images/ui/login_background.png"
          class="w-full h-screen object-cover mask-fade-bottom"
          style="object-position: center 77%;"
          alt="Hero background"
      >
      <p class="absolute top-4 right-4">{{ version }}</p>
      <div class="absolute inset-0 flex flex-col items-center justify-center">
        <h1 class="text-4xl font-bold mb-4">Caquita</h1>
        <h3>A recycling geospatial app</h3>
      </div>
    </section>

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
        <h2>Online</h2>
        <h3>{{ onlines }}</h3>
      </div>
    </section>

    <section class="p-6 md:p-24">
      <h2 class="text-2xl text-center mb-6">The game</h2>

      <p class="content">
        <strong>There is no real world recycling actions involved</strong> in the game.
        The game is a simulation, with the aim of having a database with recycling landmarks of public interest while having fun.
      </p>
      <p class="content">
        You can collect items, deposit them in the different recycling bins and get points.
        You will be able to battle another users and decorate your own virtual house at some point.
      </p>

      <button
          @click="router.push('/game')"
          class="cursor-pointer block mx-auto mt-6 px-4 py-2 modal"
      >
        Play
      </button>
    </section>
  </div>
</template>

<style scoped>

</style>