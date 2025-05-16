<script setup>
import GithubButton from 'vue-github-button'
import Hero from "@/views/home/Hero.vue";
import GlobalStats from "@/views/home/GlobalStats.vue";
import HomeFooter from "@/views/home/HomeFooter.vue";



</script>

<template>
  <div class="bg-recycle-green-500">
    <Hero/>

    <main class="p-6 flex flex-col gap-4 max-w-200 m-auto items-center">
      <h2 class="text-2xl text-center m-3 font-bold">The game</h2>

      <p class="text-center"><strong>No real-world recycling actions involved</strong> in the game, it's just a game.</p>

      <h2 class="text-2xl text-center m-3 font-bold">Introduction</h2>

      <p>You are a racoon, and the world is filled with crap. You have to find ways of getting rid of them. The most common place to  get rid of them are trash cans, which are where they are in real life.</p>

      <h2 class="text-2xl text-center m-3 font-bold">Why? What is the app's goal?</h2>
      <p>Having a public dataset of real world recycling landmarks.</p>
      <p>The issue in these kind of apps is having maintainers to keep the data up to date. A smart solution is to build a game around it so the players are the maintainers.</p>

      <h2 class="text-2xl text-center m-3 font-bold">Downloads</h2>
      <p class="text-red-500 text-center text-xs">*not ready for download, in development</p>
      <div>
        <button disabled class="erre-btn relative right-2">Android</button>
        <button disabled class="erre-btn relative left-2">iOS</button>
      </div>

      <h2 class="text-2xl text-center m-3 font-bold">Contribution portal</h2>
      <p>
        Our <router-link to="/landmarks">Landmarks</router-link> portal lets users submit trash cans, plastic containers, etc. Public debate is encouraged, for now petitions are accepted manually by me. Once a petition is accepted it gets automatically added to the game so players can interact with it.
      </p>
    </main>
  </div>

  <GithubButton
      class="fixed top-2 left-6 z-50"
      href="https://github.com/muriplz/Erre"
      data-color-scheme="no-preference: dark; light: dark; dark: dark;"
      data-icon="octicon-star" data-size="large" data-show-count="true"
      aria-label="Star muriplz/Erre on GitHub">
    Star
  </GithubButton>
  <HomeFooter/>
</template>