<script setup>

</script>

<template>
  <div class="container">
    <div class="hero">
      <h1>Caquita</h1>
      <h3>A recycling geospatial app</h3>
      <img src="/images/ui/login_background.png" alt="background" />
    </div>

    <div class="introduction">
      <h2>The game</h2>
      <p>
        There is no real world recycling actions involved in the game.
        The game is a simulation, with the aim of having a database with recycling landmarks of public interest while having fun.
      </p>
      <p>
        You can collect items, deposit them in the different recycling bins and get points.
        You will be able to battle another users and decorate your own virtual house at some point.
      </p>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.hero {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.hero img {
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  z-index: -1;
  object-fit: cover;
  object-position: center 75%;
  image-rendering: pixelated;
}

.introduction {
  margin-top: 20px;
}
</style>