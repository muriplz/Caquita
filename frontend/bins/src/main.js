import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import router from './router';
import AuthService from './js/auth/AuthService.js';
import SyncService from './js/sync/SyncService.js';
import SyncStore from './js/sync/SyncStore.js';

import settingsStore from '@/components/ui/settings/settings.js';
import { initGPSSystem } from "@/components/player/GPSTracker.js";
import Store from "@/js/Store.js";
import { createI18n } from "vue-i18n";

import enMessages from '/i18n/en_us.json'
import esMessages from '/i18n/es_es.json'
import SpawningItemStore from "./js/items/spawn/SpawningItemStore.js";
import LandmarkStore from "./js/landmarks/LandmarkStore.js";
import TrashCanStore from "./js/landmarks/trash_cans/TrashCanStore.js";
import StatsApi from "./js/stats/StatsApi.js";

const browserLanguage = navigator.language.split('-')[0];
const savedLanguage = localStorage.getItem('language') || browserLanguage || 'en';

export const i18n = createI18n({
    legacy: false,
    locale: savedLanguage,
    fallbackLocale: 'en',
    globalInjection: true,
    messages: {
        en: enMessages,
        es: esMessages
    }
})

settingsStore.init()
Store.updateItems()

StatsApi.recordView()

AuthService.validate().then(user => {
    if (Store.getUser()) {
        // Initialize sync system if user is logged in
        SyncService.init().then(() => {
            // User based
            SyncStore.init();

            // Location based
            SpawningItemStore.init();
            LandmarkStore.init();
            TrashCanStore.init();

        });
    }
});

initGPSSystem();

createApp(App)
    .use(i18n)
    .use(router)
    .mount('#app');