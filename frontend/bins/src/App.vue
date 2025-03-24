<script setup>
import Footer from './components/Footer.vue'

</script>

<template>
  <div id="app">
    <main>
      <router-view />
    </main>
  </div>
</template>

<style>
#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>