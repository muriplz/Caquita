<template>
  <div id="app" :class="{'full-screen': $route.meta.fullScreen}">
    <main>
      <router-view />
    </main>
  </div>
</template>

<style>

</style>