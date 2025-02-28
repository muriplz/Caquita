import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import router from './router';
import { createInventoryState } from './components/ui/inventory/inventoryState.js';
import AuthService from './js/auth/authService.js';
import inventoryStore from "@/components/ui/inventory/store/inventoryStore.js";

// Create inventory state before app initialization
export const inventoryState = createInventoryState();

AuthService.validate();

createApp(App)
    .use(router)
    .provide('inventoryState', inventoryState)
    .mount('#app');