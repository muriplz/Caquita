<script setup>

</script>

<template>
  <header>
    <nav>
      <router-link to="/landmarks/petitions" active-class="active" exact>
        <h1>Ongoing</h1>
      </router-link>
      <router-link to="/landmarks/accepted" active-class="active" exact>
        <h1>Accepted</h1>
      </router-link>
      <router-link to="/landmarks/rejected" active-class="active" exact>
        <h1>Rejected</h1>
      </router-link>
    </nav>
  </header>
</template>

<style scoped>

</style>