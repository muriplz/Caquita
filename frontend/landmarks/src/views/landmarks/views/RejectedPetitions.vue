<script setup>

import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
import PaginatedPetitions from "@/views/landmarks/PaginatedPetitions.vue";
</script>

<template>
  <PetitionsHeader/>

  <PaginatedPetitions status="REJECTED"/>
</template>

<style scoped>

</style>