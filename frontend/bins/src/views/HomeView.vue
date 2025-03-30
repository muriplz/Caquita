<script setup>

import router from "@/router/index.js";

async function reloadPage() {
  await router.push('/game');
}
</script>

<template>
  <div class="container">
    <div class="hero">
      <p class="version">0.0.1-alpha</p>
      <h1>Caquita</h1>
      <h3>A recycling geospatial app</h3>
      <img src="/images/ui/login_background.png" alt="background" />
    </div>

    <div class="introduction">
      <h2>The game</h2>
      <p>
        <strong>There is no real world recycling actions involved</strong> in the game.
        The game is a simulation, with the aim of having a database with recycling landmarks of public interest while having fun.
      </p>
      <p>
        You can collect items, deposit them in the different recycling bins and get points.
        You will be able to battle another users and decorate your own virtual house at some point.
      </p>

      <div class="play-button">
        <div class="button" @click="reloadPage">Play</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero {
  position: relative;
  height: 100vh;
  width: 100%;
}

.version {
  position: absolute;
  top: 0;
  right: 0;
  padding: 10px;
  color: white;
}

.hero h1 {
  margin-top: 19vh;
}

.hero img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  max-height: 100vh;
  object-fit: cover;
  object-position: center 88%;
  z-index: -1;
  image-rendering: pixelated;
  mask-image: linear-gradient(to bottom, black 80%, transparent 100%);
}

.introduction {
  margin-top: 40px;
  padding: 20px;
  color: white;
  line-height: 1.6;
  text-align: justify;
  text-align-last: center;
}

.introduction p {
  color: #aaaaaa;
}

.play-button {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.play-button .button {
  border-radius: 8px;
  padding: 0.6em 1.2em;
  font-size: 1em;
  background-color: #1a1a1a;
  color: white;
  text-decoration: none;
  cursor: pointer;
  transition: border-color 0.25s;
}

.play-button .button:hover {
  border-color: #646cff;
}
</style>