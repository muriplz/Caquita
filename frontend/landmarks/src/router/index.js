// src/router/index.js
import {createRouter, createWebHistory} from 'vue-router';
import HomeView from "@/views/HomeView.vue";
import LoginView from "@/views/LoginView.vue";
import LandmarksView from "@/views/LandmarksView.vue";
import AcceptedPetitions from "@/views/landmarks/views/AcceptedPetitions.vue";
import RejectedPetitions from "@/views/landmarks/views/RejectedPetitions.vue";
import PendingPetitions from "@/views/landmarks/views/PendingPetitions.vue";
import CreatePetition from "@/views/landmarks/CreatePetition.vue";
import PetitionPage from "@/views/landmarks/petition_page/PetitionPage.vue";
import AuthService from "../js/auth/authService";

const routes = [
    { path: '/', component: HomeView},

    { path: '/login', component: LoginView},

    { path: '/landmarks', component: LandmarksView },
    { path: '/landmarks/accepted', component: AcceptedPetitions, meta: { requiresAuth: true } },
    { path: '/landmarks/rejected', component: RejectedPetitions, meta: { requiresAuth: true } },
    { path: '/landmarks/pending', component: PendingPetitions, meta: { requiresAuth: true } },
    { path: '/landmarks/create', component: CreatePetition, meta: { requiresAuth: true } },
    { path: '/landmarks/:id', component: PetitionPage, meta: { requiresAuth: true } }
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