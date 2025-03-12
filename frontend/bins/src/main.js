import {createApp} from 'vue';
import './style.css';
import App from './App.vue';
import router from './router';
import AuthService from './js/auth/authService.js';

import {CarbonIconsVue, LogoDiscord32, LogoGithub32, Settings32, User32, UserProfile32,} from '@carbon/icons-vue';

import settingsStore from '@/components/ui/settings/settings.js';
import {initGPSSystem} from "@/components/player/GPSTracker.js";

settingsStore.init();

AuthService.validate();
initGPSSystem();

createApp(App)
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