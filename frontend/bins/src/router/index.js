// src/router/index.js
import {createRouter, createWebHistory} from 'vue-router';
import MapView from '../views/MapView.vue';
import Onboarding from "@/views/Onboarding.vue";

const routes = [
    { path: '/', component: MapView },
    { path: '/onboarding', component: Onboarding },
];

const router = createRouter({
    history: createWebHistory(),
    routes: routes
});

export default router;