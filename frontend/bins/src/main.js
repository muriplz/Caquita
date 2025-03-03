import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import router from './router';
import { createInventoryState } from './components/ui/inventory/inventoryState.js';
import AuthService from './js/auth/authService.js';

import {
    CarbonIconsVue,
    LogoDiscord16,
    LogoGithub16,
    Settings32,
} from '@carbon/icons-vue';

import settingsStore from '@/components/ui/settings/settings.js';

settingsStore.init();

export const inventoryState = createInventoryState();

AuthService.validate();

createApp(App)
    .use(router)
    .use(
        CarbonIconsVue,
        {
            components: {
                DiscordLogo: LogoDiscord16,
                GithubLogo: LogoGithub16,
                SettingsIcon: Settings32,
            }
        }
    )
    .provide('inventoryState', inventoryState)
    .mount('#app');