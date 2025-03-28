import { reactive } from 'vue'
import Store from "@/js/Store.js"

const ANIMATION_DURATION = 200;
const ANIMATION_BUFFER = 20;

const state = reactive({
    currentScreen: null,
    previousScreen: null,
    history: [],
    navigationStack: [],
    isMenuOpen: false,
    globalElements: {
        showSettingsButton: true,
        showPlusButton: true,
        showProfileButton: true,
        showCurrencyPanel: true
    },
    animationDirection: 'forward',
    lastNavigationTime: 0,
    isNavigating: false,
    beforeSettingsScreen: null,
    isMainMenuActive: true,
    isFirstNavigation: true,
    isAnimating: false,
    menuIsVisible: true,
    targetScreen: null
})

const screens = {
    MAIN_MENU: {
        name: 'MAIN_MENU',
        showSettingsButton: true,
        showPlusButton: true,
        showProfileButton: true,
        showCurrencyPanel: true,
        parent: null
    },
    INVENTORY: {
        name: 'INVENTORY',
        showSettingsButton: false,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: true,
        parent: 'MAIN_MENU'
    },
    VITRINE: {
        name: 'VITRINE',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: true,
        parent: 'MAIN_MENU'
    },
    NOIDEA: {
        name: 'NOIDEA',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: false,
        parent: 'MAIN_MENU'
    },
    SETTINGS: {
        name: 'SETTINGS',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: false,
        parent: null
    },
    ABOUT: {
        name: 'ABOUT',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: true,
        parent: 'MAIN_MENU'
    },
    CONTRIBUTING: {
        name: 'CONTRIBUTING',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: false,
        parent: 'MAIN_MENU'
    },
    PROFILE: {
        name: 'PROFILE',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: true,
        parent: 'MAIN_MENU'
    },
    ITEM_INFO: {
        name: 'ITEM_INFO',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        showCurrencyPanel: true,
        parent: 'INVENTORY'
    }
}

function findScreenInHistory(screenName) {
    return state.history.findIndex(screen => screen === screenName);
}

function canNavigate() {
    if (state.isNavigating || state.isAnimating) {
        return false;
    }

    const now = Date.now();
    if (now - state.lastNavigationTime < 150) {
        return false;
    }
    state.lastNavigationTime = now;
    return true;
}

function updateGlobalElements(screen) {
    if (screen) {
        state.globalElements.showSettingsButton = screen.showSettingsButton;
        state.globalElements.showPlusButton = screen.showPlusButton;
        state.globalElements.showProfileButton = screen.showProfileButton;
        state.globalElements.showCurrencyPanel = screen.showCurrencyPanel;
    } else {
        state.globalElements.showSettingsButton = true;
        state.globalElements.showPlusButton = true;
        state.globalElements.showProfileButton = true;
        state.globalElements.showCurrencyPanel = true;
    }
}

function getScreenPath(screenName) {
    if (!screenName || screenName === 'MAIN_MENU') return [];

    const path = [screenName];
    let currentScreen = screens[screenName];

    while (currentScreen && currentScreen.parent && currentScreen.parent !== 'MAIN_MENU') {
        path.unshift(currentScreen.parent);
        currentScreen = screens[currentScreen.parent];
    }

    return path;
}

function animateToFrom(from, to) {
    state.isAnimating = true;
    state.targetScreen = to;

    if (to === 'MAIN_MENU') {
        state.menuIsVisible = false;
    }

    if (from === 'MAIN_MENU') {
        state.menuIsVisible = false;
    }

    state.previousScreen = from === 'MAIN_MENU' ? null : from;

    if (to === 'MAIN_MENU') {
        state.animationDirection = 'backward';

        setTimeout(() => {
            state.currentScreen = null;
            state.isMainMenuActive = true;
            state.menuIsVisible = true;
            state.isAnimating = false;
            state.targetScreen = null;
        }, ANIMATION_DURATION);
    } else {
        state.animationDirection = from === 'MAIN_MENU' ? 'forward' : state.animationDirection;

        if (from === 'MAIN_MENU') {
            state.currentScreen = '_PLACEHOLDER_';
        }

        setTimeout(() => {
            state.currentScreen = to;
            state.isMainMenuActive = false;
            state.isAnimating = false;
            state.menuIsVisible = false;
            state.targetScreen = null;
        }, ANIMATION_DURATION);
    }

    updateGlobalElements(to === 'MAIN_MENU' ? null : screens[to]);
}

export default {
    state,
    screens,

    openMenu() {
        state.isMenuOpen = true;
        document.body.style.overflow = 'hidden';
        state.currentScreen = null;
        state.isMainMenuActive = true;
        state.menuIsVisible = true;
    },

    closeMenu() {
        state.isMenuOpen = false;
        document.body.style.overflow = '';
        state.currentScreen = null;
        state.previousScreen = null;
        state.navigationStack = [];
        state.history = [];
        state.isNavigating = false;
        state.isAnimating = false;
        state.beforeSettingsScreen = null;
        state.isMainMenuActive = true;
        state.isFirstNavigation = true;
        state.menuIsVisible = true;
        state.targetScreen = null;
    },

    navigate(screenName) {
        if (!screenName || !screens[screenName]) {
            console.error(`Screen ${screenName} does not exist or is invalid`);
            return false;
        }

        if (!canNavigate()) return false;

        if (!state.isMenuOpen) {
            this.openMenu();
        }

        if (screenName === 'MAIN_MENU') {
            if (state.currentScreen) {
                animateToFrom(state.currentScreen, 'MAIN_MENU');
            } else {
                state.currentScreen = null;
                state.isMainMenuActive = true;
                state.menuIsVisible = true;
            }

            state.history = [];
            state.navigationStack = [];

            return true;
        }

        if (!state.currentScreen) {
            state.isFirstNavigation = false;

            const screenPath = getScreenPath(screenName);
            state.navigationStack = screenPath;

            state.history.push(screenName);

            animateToFrom('MAIN_MENU', screenName);

            return true;
        }

        const existingScreenIndex = findScreenInHistory(screenName);

        if (existingScreenIndex >= 0) {
            state.animationDirection = 'backward';
            state.history = state.history.slice(0, existingScreenIndex + 1);
        } else {
            state.animationDirection = 'forward';
            state.history.push(screenName);
        }

        const screenPath = getScreenPath(screenName);
        state.navigationStack = screenPath;

        state.isNavigating = true;
        state.previousScreen = state.currentScreen;
        state.currentScreen = screenName;
        state.isMainMenuActive = false;
        state.menuIsVisible = false;

        updateGlobalElements(screens[screenName]);

        setTimeout(() => {
            state.isNavigating = false;
        }, ANIMATION_DURATION + ANIMATION_BUFFER);

        return true;
    },

    toggleSettings() {
        if (state.currentScreen === 'SETTINGS') {
            if (state.beforeSettingsScreen) {
                this.navigate(state.beforeSettingsScreen);
                state.beforeSettingsScreen = null;
            } else if (state.isMainMenuActive) {
                this.navigate('MAIN_MENU');
            } else {
                this.navigate('MAIN_MENU');
            }
        } else {
            state.beforeSettingsScreen = state.currentScreen;

            if (!state.currentScreen) {
                state.isMainMenuActive = true;
            }

            this.navigate('SETTINGS');
        }
    },

    goBack() {
        if (!canNavigate()) return false;

        if (state.currentScreen === 'SETTINGS') {
            if (state.beforeSettingsScreen) {
                this.navigate(state.beforeSettingsScreen);
                state.beforeSettingsScreen = null;
                return true;
            } else if (state.isMainMenuActive) {
                this.navigate('MAIN_MENU');
                return true;
            }
        }

        if (state.navigationStack.length > 1) {
            state.navigationStack.pop();

            const parentScreen = state.navigationStack[state.navigationStack.length - 1];

            if (parentScreen && screens[parentScreen]) {
                state.animationDirection = 'backward';

                if (!state.history.includes(parentScreen)) {
                    state.history.push(parentScreen);
                }

                state.isNavigating = true;
                state.previousScreen = state.currentScreen;
                state.currentScreen = parentScreen;
                state.menuIsVisible = false;

                updateGlobalElements(screens[parentScreen]);

                setTimeout(() => {
                    state.isNavigating = false;
                }, ANIMATION_DURATION + ANIMATION_BUFFER);

                return true;
            }
        }

        if (state.history.length > 1) {
            state.history.pop();
            const previousScreen = state.history[state.history.length - 1];

            if (previousScreen && screens[previousScreen]) {
                state.animationDirection = 'backward';

                state.isNavigating = true;
                state.previousScreen = state.currentScreen;
                state.currentScreen = previousScreen;
                state.menuIsVisible = false;

                updateGlobalElements(screens[previousScreen]);

                setTimeout(() => {
                    state.isNavigating = false;
                }, ANIMATION_DURATION + ANIMATION_BUFFER);

                return true;
            }
        }

        if (state.currentScreen) {
            animateToFrom(state.currentScreen, 'MAIN_MENU');

            state.history = [];
            state.navigationStack = [];

            return true;
        }

        this.closeMenu();
        return false;
    },

    goToMainMenu() {
        if (!state.isMenuOpen) {
            this.openMenu();
        }

        this.navigate('MAIN_MENU');
        return true;
    },

    isActive(screenName) {
        if (screenName === 'MAIN_MENU') {
            return state.currentScreen === null && state.isMenuOpen;
        }
        return state.currentScreen === screenName;
    },

    isInNavigationPath(screenName) {
        if (screenName === 'MAIN_MENU') {
            return true;
        }
        return state.navigationStack.includes(screenName);
    },

    getParentScreen() {
        if (!state.currentScreen || !screens[state.currentScreen]) return null;
        return screens[state.currentScreen].parent;
    },

    reset() {
        state.currentScreen = null;
        state.previousScreen = null;
        state.history = [];
        state.navigationStack = [];
        state.isNavigating = false;
        state.isAnimating = false;
        state.isMenuOpen = false;
        state.beforeSettingsScreen = null;
        state.isMainMenuActive = true;
        state.isFirstNavigation = true;
        state.menuIsVisible = true;
        state.targetScreen = null;
        document.body.style.overflow = '';
    },

    toggleCurrencyPanel(show) {
        if (show !== undefined) {
            state.globalElements.showCurrencyPanel = show;
        } else {
            state.globalElements.showCurrencyPanel = !state.globalElements.showCurrencyPanel;
        }
    }
}