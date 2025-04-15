<script setup>

import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
</script>

<template>
  <PetitionsHeader/>

  <div class="container">
    <div class="introduction">
      <h3>A public archive</h3>
      <p>Anyone can contribute to adding Landmarks of cans, containers, etc.<br/> And everyone can retrieve the data from
      <a href="https://townhall.caquita.app">
        Townhall
      </a>Portal. </p>
    </div>

    <div class="introduction">
      <h3>About the Game</h3>
      <p>
        Any petition that gets <router-link to="/landmarks/accepted">accepted</router-link> will be available in the game.
        <br/>
        It will also give 200 xp and 100 beans to the user that submitted it.
      </p>
    </div>
  </div>
</template>

<style scoped>
.container {
  margin: 160px 0 0 0;
}

.introduction {
  background: #fff;
  border: 4px solid #979797;
  padding: 6px 16px;
  margin-bottom: 20px;
}

@media (max-width: 600px) {
  .container {
    margin: 290px auto 0 auto;
  }
}
</style>