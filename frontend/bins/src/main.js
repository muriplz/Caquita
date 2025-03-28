import {createApp} from 'vue';
import './style.css';
import App from './App.vue';
import router from './router';
import AuthService from './js/auth/AuthService.js';
import SyncService from './js/sync/SyncService.js';

import {CarbonIconsVue, LogoDiscord32, LogoGithub32, Settings32, User32, UserProfile32,} from '@carbon/icons-vue';

import settingsStore from '@/components/ui/settings/settings.js';
import {initGPSSystem} from "@/components/player/GPSTracker.js";
import Store from "@/js/Store.js";
import {createI18n} from "vue-i18n";

import enMessages from '/i18n/en_us.json'
import esMessages from '/i18n/es_es.json'

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

settingsStore.init();
Store.updateItems();

AuthService.validate().then(user => {
    if (user) {
        // Initialize sync system if user is logged in
        SyncService.init();
    }
});

initGPSSystem();

createApp(App)
    .use(i18n)
    .use(router)
    .use(
        CarbonIconsVue,
        {
            components: {
                DiscordLogo: LogoDiscord32,
                GithubLogo: LogoGithub32,
                SettingsIcon: Settings32,
                UserIcon: User32,
                UserProfileIcon: UserProfile32,
            }
        }
    )
    .mount('#app');