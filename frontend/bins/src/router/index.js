// src/router/index.js
import {createRouter, createWebHistory} from 'vue-router';
import MapView from '../views/MapView.vue';
import HomeView from "@/views/HomeView.vue";
import AuthService from "@/js/auth/AuthService.js";
import LoginView from "@/views/LoginView.vue";
import LandmarksView from "@/views/LandmarksView.vue";
import AcceptedPetitions from "@/views/landmarks/views/AcceptedPetitions.vue";
import RejectedPetitions from "@/views/landmarks/views/RejectedPetitions.vue";
import OngoingPetitions from "@/views/landmarks/views/OngoingPetitions.vue";
import CreatePetition from "@/views/landmarks/CreatePetition.vue";

const routes = [
    { path: '/', component: HomeView},
    { path: '/login', component: LoginView},
    { path: '/game', component: MapView, meta: { requiresAuth: true }},
    { path: '/landmarks', component: LandmarksView },
    { path: '/landmarks/accepted', component: AcceptedPetitions, meta: { requiresAuth: true } },
    { path: '/landmarks/rejected', component: RejectedPetitions, meta: { requiresAuth: true } },
    { path: '/landmarks/ongoing', component: OngoingPetitions, meta: { requiresAuth: true } },
    { path: '/landmarks/create', component: CreatePetition, meta: { requiresAuth: true } }
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