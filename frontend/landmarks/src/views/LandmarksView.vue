<script setup>

import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
</script>

<template>
  <PetitionsHeader/>

  <div class="mt-50">
    <div class="bg-white p-4 m-4 text-center border-gray-500 border-3">
      <h3 class="text-xl mb-3 font-bold">A public archive</h3>
      <p>Anyone can contribute to adding Landmarks of trash cans, plastic containers, etc.<br/> And everyone can retrieve the data from
      <a href="https://townhall.caquita.app">
        Townhall
      </a>Portal. </p>
    </div>

    <div class="bg-white p-4 m-4 text-center border-gray-500 border-3">
      <h3 class="text-xl mb-3 font-bold">About the Game</h3>
      <p>
        Any petition that gets <router-link to="/landmarks/accepted">accepted</router-link> will be available in the game.
      </p>
    </div>
  </div>
</template>

<style scoped>

</style>