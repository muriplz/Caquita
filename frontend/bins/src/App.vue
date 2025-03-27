<script setup></script>

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
  height: 100vh;
  max-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

body, html {
  margin: 0;
  padding: 0;
  overflow: hidden;
  height: 100vh;
  width: 100vw;
}
</style>