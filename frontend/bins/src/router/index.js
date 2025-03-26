// src/router/index.js
import {createRouter, createWebHistory} from 'vue-router';
import MapView from '../views/MapView.vue';
import Onboarding from "@/views/Onboarding.vue";
import HomeView from "@/views/HomeView.vue";
import AuthService from "@/js/auth/authService.js";
import LoginView from "@/views/LoginView.vue";

const routes = [
    { path: '/', component: HomeView},
    { path: '/login', component: LoginView},
    { path: '/game', component: MapView, meta: { requiresAuth: true }},
    { path: '/onboarding', component: Onboarding },
];

const router = createRouter({
    history: createWebHistory(),
    routes: routes
});

export default router;

router.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!AuthService.getToken() || !AuthService.validate()) {
            next('/login');
        } else {
            next();
        }
    } else {
        next();
    }
});