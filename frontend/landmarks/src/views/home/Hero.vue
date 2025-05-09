<script setup>
</script>

<template>
  <section class="relative min-h-screen ">
    <img
        src="/images/login_background.png"
        class="w-screen h-screen object-cover mask-fade-bottom"
        style="object-position: center 77%;"
        alt="Hero background"
    >
    <p class="absolute top-2 right-8">{{ version }}</p>
    <div class="absolute inset-0 flex flex-col items-center justify-center">
      <h1 class="text-4xl font-bold mb-4">Caquita</h1>
      <h3>A recycling geospatial app</h3>
    </div>
  </section>
</template>