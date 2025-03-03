import { reactive, watch } from 'vue';

// Storage key for all settings
const STORAGE_KEY = 'user-settings';

// Default settings structure
const defaultSettings = {
    general: {
        gpsEnabled: false,
        language: 'en',
    },
    controls: {
        invertYAxis: false,
        cameraSensitivityX: 5,
        cameraSensitivityY: 2,
        zoomSpeed: 1,
    },
    graphics: {
        renderDistance: 4,
    }
};

// Reactive settings object that components can use
const settings = reactive({...JSON.parse(JSON.stringify(defaultSettings))});

// Load settings from localStorage
function loadSettings() {
    try {
        const savedSettings = localStorage.getItem(STORAGE_KEY);
        if (savedSettings) {
            const parsed = JSON.parse(savedSettings);

            // Merge saved settings with defaults to handle new settings
            Object.keys(defaultSettings).forEach(category => {
                if (parsed[category]) {
                    Object.keys(defaultSettings[category]).forEach(key => {
                        if (parsed[category][key] !== undefined) {
                            settings[category][key] = parsed[category][key];
                        }
                    });
                }
            });
        }
    } catch (e) {
        console.error('Error loading settings:', e);
    }
}

// Save settings to localStorage
function saveSettings() {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
    } catch (e) {
        console.error('Error saving settings:', e);
    }
}

// Reset all settings to defaults
function resetAllSettings() {
    try {
        // Clear localStorage
        localStorage.removeItem(STORAGE_KEY);

        // Reset the reactive store to defaults
        const freshDefaults = JSON.parse(JSON.stringify(defaultSettings));

        // Update each property individually to maintain reactivity
        Object.keys(freshDefaults).forEach(category => {
            Object.keys(freshDefaults[category]).forEach(key => {
                settings[category][key] = freshDefaults[category][key];
            });
        });

        return true;
    } catch (e) {
        console.error('Error resetting settings:', e);
        return false;
    }
}

// Initialize settings and watch for changes
function init() {
    loadSettings();

    // Set up watcher to auto-save when settings change
    watch(settings, () => {
        saveSettings();
    }, { deep: true });
}

// Get a fresh copy of default settings
function getDefaultSettings() {
    return JSON.parse(JSON.stringify(defaultSettings));
}

export default {
    settings,
    init,
    saveSettings,
    resetAllSettings,
    getDefaultSettings
};