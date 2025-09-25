<script setup>
import router from "@/router/index.js";
import { ref, onMounted, onUnmounted } from 'vue';
import {useUserStore} from "@/js/Store.js";

const store = useUserStore();
</script>

<template>
  <Transition name="header-slide">
    <header class="w-full shadow fixed top-0 left-0 z-50 flex justify-between">
      <div class="mx-4 z-50">
        <h4 v-if="store.getUser">@{{ store.getUser.username }}</h4>
        <a v-else @click="router.push('/login')" class="cursor-pointer">Login</a>
      </div>
      <div class="bg-gray-100 w-full absolute left-1/2 -translate-x-1/2 flex flex-col items-center mb-3">
        <router-link to="/landmarks" class="mb-3 text-3xl sm:text-5xl" exact>Landmarks</router-link>
        <nav class="flex justify-center gap-5">
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
      <div class="flex items-center pr-5 z-50">
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
  transition: transform 0.15s ease;
}

.header-slide-enter-from,
.header-slide-leave-to {
  transform: translateY(-100%);
}
</style>