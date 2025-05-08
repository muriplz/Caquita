import { createApp } from 'vue';
import './global.css';
import './images.css';
import App from './App.vue';
import router from './router';
import AuthService from './js/auth/AuthService.js';
import { createI18n } from "vue-i18n";

import enMessages from '/i18n/en_us.json'
import esMessages from '/i18n/es_es.json'
import StatsApi from "./js/stats/StatsApi.js";
import {createPinia} from "pinia";
import {useUserStore} from "@/js/Store.js";

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

StatsApi.recordView()

AuthService.validate().then(user => {
    if (useUserStore().getUser()) {
    }
});

const pinia = createPinia()

createApp(App)
    .use(i18n)
    .use(router)
    .use(pinia)
    .mount('#app');