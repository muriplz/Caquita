<script setup>
import Store from "@/js/Store.js";
import router from "@/router/index.js";
</script>

<template>
  <header class="header">
    <div class="left-section">
      <h4 v-if="Store.getUser()">@{{ Store.getUser().username }}</h4>
      <a @click="router.push('/game')">Back to the game</a>
    </div>
    <div class="container">
      <router-link to="/landmarks" class="title" exact>Landmarks</router-link>
      <nav class="nav">
        <router-link to="/landmarks/ongoing" active-class="active" exact>
          <h1>Ongoing</h1>
        </router-link>
        <router-link to="/landmarks/accepted" active-class="active" exact>
          <h1>Accepted</h1>
        </router-link>
        <router-link to="/landmarks/rejected" active-class="active" exact>
          <h1>Rejected</h1>
        </router-link>
      </nav>
    </div>
    <div class="right-section">
      <div class="create-petition-button" @click="router.push('/landmarks/create')">Create petition</div>
    </div>
  </header>
</template>

<style scoped>
.title {
  margin-bottom: 12px;
  font-size: 48px;
  text-decoration: none;
  color: #333;
}

.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  margin-bottom: 12px;
}

.header {
  width: 100%;
  background-color: #f8f8f8;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.left-section {
  display: flex;
  align-items: flex-start;
  padding-left: 20px;
  min-width: 150px;
  flex-direction: column;
  justify-content: space-between;
  padding-bottom: 26px;
}


.nav {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.nav a {
  text-decoration: none;
  color: #333;
  padding: 8px 16px;
  border-radius: 4px;
  user-select: none;
  -webkit-user-drag: none;
}

.nav a.active {
  background-color: #e0e0e0;
  color: #000;
}

.nav h1 {
  font-size: 16px;
  margin: 0;
}

.right-section {
  display: flex;
  align-items: center;
  padding-right: 20px;

}

.create-petition-button {
  background-color: #d3a263;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

@media(max-width: 480px) {
  .header {
    flex-direction: column;
  }

  .left-section {
    flex-direction: row;
    align-items: center;
    padding: 0 20px;
  }

  .nav {
    margin-top: 12px;
  }
}
</style>