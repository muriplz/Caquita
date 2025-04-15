<template>
  <div class="modal px-6 py-3">
    <div class="flex justify-center gap-4 items-end mb-4">
      <h1 class="text-3xl">About Us</h1>
      <ChangelogMarkdown class="changelog"/>
    </div>

    <div class="about-content">
      <p>Welcome to Caquita. A game about inventory management with real world recycling landmarks.</p>

      <p>Find our monorepo on
      <a href="https://github.com/muriplz/Caquita" class="underline">Github</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import ChangelogMarkdown from "@/components/ChangelogMarkdown.vue";
</script>

<style scoped>

</style>