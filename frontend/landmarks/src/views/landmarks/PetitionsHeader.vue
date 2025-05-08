<script setup>
import router from "@/router/index.js";
import { ref, onMounted, onUnmounted } from 'vue';
import {useUserStore} from "@/js/Store.js";

const store = useUserStore();

const isVisible = ref(true);
const scrollThreshold = 30;

const handleScroll = () => {
  isVisible.value = window.scrollY <= scrollThreshold;
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<template>
  <Transition name="header-slide">
    <header v-show="isVisible" class="header">
      <div class="left-section">
        <h4 v-if="store.getUser()">@{{ store.getUser() }}</h4>
        <a @click="router.push('/game')">Back to the game</a>
      </div>
      <div class="container">
        <router-link to="/landmarks" class="title" exact>Landmarks</router-link>
        <nav class="nav">
          <router-link to="/landmarks/pending" active-class="active" exact>
            <h1>Pending</h1>
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
        <router-link to="/landmarks/create" class="submit-button" exact>
          <h4>Submit</h4>
        </router-link>
      </div>
    </header>
  </Transition>
</template>

<style scoped>
.title {
  margin-bottom: 12px;
  font-size: 48px;
  text-decoration: none;
  color: #333;
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
  align-items: center;
}

.container {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 12px;
}

.left-section {
  display: flex;
  align-items: flex-start;
  padding-left: 20px;
  min-width: 150px;
  flex-direction: column;
  justify-content: space-between;
  padding-bottom: 26px;
  gap: 30px;
}

.left-section a {
  cursor: pointer;
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

.submit-button {
  text-decoration: none;
  color: #333;
  border-radius: 4px;
  user-select: none;
  -webkit-user-drag: none;
}

.header-slide-enter-active,
.header-slide-leave-active {
  transition: transform 0.3s ease;
}

.header-slide-enter-from,
.header-slide-leave-to {
  transform: translateY(-100%);
}

@media(max-width: 480px) {
  .header {
    flex-direction: column;
    align-items: center;
  }

  .left-section {
    flex-direction: row;
    align-items: center;
    padding: 0 20px;
    width: 100%;
    justify-content: space-between;
  }

  .container {
    position: static;
    transform: none;
    margin-bottom: 12px;
    width: 100%;
  }

  .submit-button {
    margin: 0 auto;
    padding: 0;
  }

  .nav {
    margin-top: 12px;
    gap: 25px;
    width: 100%;
    justify-content: center;
  }

  .nav a {
    padding: 4px 8px;
  }
}
</style>