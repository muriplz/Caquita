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
    <header v-show="isVisible" class="w-full bg-gray-100 shadow fixed top-0 left-0 z-50 flex justify-between items-center max-sm:flex-col max-sm:items-center">
      <div class="flex items-start pl-5 min-w-[150px] flex-col justify-between pb-6 gap-8 max-sm:flex-row max-sm:items-center max-sm:p-0 max-sm:px-5 max-sm:w-full max-sm:justify-between">
        <h4 v-if="store.getUser">@{{ store.getUser.username }}</h4>
        <a v-else @click="router.push('/login')" class="cursor-pointer">Login</a>
      </div>
      <div class="absolute left-1/2 -translate-x-1/2 flex flex-col items-center mb-3 max-sm:static max-sm:transform-none max-sm:mb-3 max-sm:w-full max-sm:left-auto max-sm:translate-x-0">
        <router-link to="/landmarks" class="mb-3 text-5xl" exact>Landmarks</router-link>
        <nav class="flex justify-center gap-5 max-sm:mt-3 max-sm:gap-3 max-sm:w-full max-sm:justify-center">
          <router-link to="/landmarks/pending" active-class="bg-gray-300" class="px-4 py-2" exact>
            <h1 class="text-base m-0">Pending</h1>
          </router-link>
          <router-link to="/landmarks/accepted" active-class="bg-gray-300" class="px-4 py-2" exact>
            <h1 class="text-base m-0">Accepted</h1>
          </router-link>
          <router-link to="/landmarks/rejected" active-class="bg-gray-300" class="px-4 py-2" exact>
            <h1 class="text-base m-0">Rejected</h1>
          </router-link>
        </nav>
      </div>
      <div class="flex items-center pr-5 max-sm:mb-4">
        <router-link to="/landmarks/create" exact>
          <h4>Submit</h4>
        </router-link>
      </div>
    </header>
  </Transition>
</template>

<style scoped>
.header-slide-enter-active,
.header-slide-leave-active {
  transition: transform 0.3s ease;
}

.header-slide-enter-from,
.header-slide-leave-to {
  transform: translateY(-100%);
}
</style>