import { reactive } from 'vue'
import Store from "@/js/Store.js"

// Animation duration constants
const ANIMATION_DURATION = 200;  // ms
const ANIMATION_BUFFER = 20;     // additional ms

const state = reactive({
    currentScreen: null,
    previousScreen: null,
    history: [],
    navigationStack: [],
    isMenuOpen: false,
    globalElements: {
        showSettingsButton: true,
        showPlusButton: true,
        showProfileButton: true
    },
    animationDirection: 'forward',
    lastNavigationTime: 0,
    isNavigating: false,
    beforeSettingsScreen: null,
    isMainMenuActive: true,
    isFirstNavigation: true,

    // Animation state
    isAnimating: false,
    menuIsVisible: true,
    targetScreen: null     // Used during animations to maintain proper flow
})

// Screen definitions with parent-child relationships
const screens = {
    // MAIN_MENU is a logical state, not an actual component screen
    MAIN_MENU: {
        name: 'MAIN_MENU',
        showSettingsButton: true,
        showPlusButton: true,
        showProfileButton: true,
        parent: null
    },
    INVENTORY: {
        name: 'INVENTORY',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        parent: 'MAIN_MENU'
    },
    VITRINE: {
        name: 'VITRINE',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        parent: 'MAIN_MENU'
    },
    NOIDEA: {
        name: 'NOIDEA',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        parent: 'MAIN_MENU'
    },
    SETTINGS: {
        name: 'SETTINGS',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        parent: null
    },
    ABOUT: {
        name: 'ABOUT',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        parent: 'MAIN_MENU'
    },
    CONTRIBUTING: {
        name: 'CONTRIBUTING',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        parent: 'MAIN_MENU'
    },
    PROFILE: {
        name: 'PROFILE',
        showSettingsButton: true,
        showPlusButton: false,
        showProfileButton: true,
        parent: 'MAIN_MENU'
    }
}

// Find screen in history
function findScreenInHistory(screenName) {
    return state.history.findIndex(screen => screen === screenName);
}

// Prevent navigation events that are too close together
function canNavigate() {
    // Don't allow navigation if already navigating or animating
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

// Update UI elements based on screen config
function updateGlobalElements(screen) {
    if (screen) {
        state.globalElements.showSettingsButton = screen.showSettingsButton;
        state.globalElements.showPlusButton = screen.showPlusButton;
        state.globalElements.showProfileButton = screen.showProfileButton;
    } else {
        // Default values for main menu
        state.globalElements.showSettingsButton = true;
        state.globalElements.showPlusButton = true;
        state.globalElements.showProfileButton = true;
    }
}

// Get full path to a screen (including parents)
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

// Helper to animate transitions to or from main menu
function animateToFrom(from, to) {
    // Start animation
    state.isAnimating = true;
    state.targetScreen = to;

    // If going to main menu, hide menu immediately
    if (to === 'MAIN_MENU') {
        state.menuIsVisible = false;
    }

    // If coming from main menu, make sure screen is ready
    if (from === 'MAIN_MENU') {
        state.menuIsVisible = false;
    }

    // Set current screen for animation
    state.previousScreen = from === 'MAIN_MENU' ? null : from;

    if (to === 'MAIN_MENU') {
        // Animate to main menu
        state.animationDirection = 'backward';

        setTimeout(() => {
            // After animation completes, update states
            state.currentScreen = null;
            state.isMainMenuActive = true;
            state.menuIsVisible = true;
            state.isAnimating = false;
            state.targetScreen = null;
        }, ANIMATION_DURATION);
    } else {
        // Animate to target screen
        state.animationDirection = from === 'MAIN_MENU' ? 'forward' : state.animationDirection;

        // Set current screen for animation if coming from main menu
        if (from === 'MAIN_MENU') {
            // Placeholder screen to animate from
            state.currentScreen = '_PLACEHOLDER_';
        }

        setTimeout(() => {
            // After animation completes, update states
            state.currentScreen = to;
            state.isMainMenuActive = false;
            state.isAnimating = false;
            state.menuIsVisible = false;
            state.targetScreen = null;
        }, ANIMATION_DURATION);
    }

    // Update UI elements immediately
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
        // Validate input
        if (!screenName || !screens[screenName]) {
            console.error(`Screen ${screenName} does not exist or is invalid`);
            return false;
        }

        if (!canNavigate()) return false;

        // Open menu if closed
        if (!state.isMenuOpen) {
            this.openMenu();
        }

        // Handle special case for MAIN_MENU
        if (screenName === 'MAIN_MENU') {
            // Animate to main menu if we're on a screen
            if (state.currentScreen) {
                animateToFrom(state.currentScreen, 'MAIN_MENU');
            } else {
                // Already at main menu, just update state
                state.currentScreen = null;
                state.isMainMenuActive = true;
                state.menuIsVisible = true;
            }

            // Reset history
            state.history = [];
            state.navigationStack = [];

            return true;
        }

        // Special handling for first navigation from main menu
        if (!state.currentScreen) {
            state.isFirstNavigation = false;

            // Update navigation stack for proper nesting
            const screenPath = getScreenPath(screenName);
            state.navigationStack = screenPath;

            // Add to history
            state.history.push(screenName);

            // Animate from main menu to screen
            animateToFrom('MAIN_MENU', screenName);

            return true;
        }

        // Check if we're returning to a previously visited screen
        const existingScreenIndex = findScreenInHistory(screenName);

        if (existingScreenIndex >= 0) {
            // Going back to a screen we've already visited
            state.animationDirection = 'backward';

            // Trim history to this point
            state.history = state.history.slice(0, existingScreenIndex + 1);
        } else {
            // Going to a new screen
            state.animationDirection = 'forward';
            state.history.push(screenName);
        }

        // Update navigation stack for proper nesting
        const screenPath = getScreenPath(screenName);
        state.navigationStack = screenPath;

        // Start navigation
        state.isNavigating = true;
        state.previousScreen = state.currentScreen;
        state.currentScreen = screenName;
        state.isMainMenuActive = false;
        state.menuIsVisible = false;

        // Update UI elements
        updateGlobalElements(screens[screenName]);

        // Reset navigation flag after a delay
        setTimeout(() => {
            state.isNavigating = false;
        }, ANIMATION_DURATION + ANIMATION_BUFFER);

        return true;
    },

    toggleSettings() {
        if (state.currentScreen === 'SETTINGS') {
            // Going back from settings to previous screen
            if (state.beforeSettingsScreen) {
                this.navigate(state.beforeSettingsScreen);
                state.beforeSettingsScreen = null;
            } else if (state.isMainMenuActive) {
                // If we were at main menu before settings, go back to main menu
                this.navigate('MAIN_MENU');
            } else {
                // Fallback to main menu
                this.navigate('MAIN_MENU');
            }
        } else {
            // Going to settings, store current screen
            state.beforeSettingsScreen = state.currentScreen;

            // Also track if we're at main menu
            if (!state.currentScreen) {
                state.isMainMenuActive = true;
            }

            this.navigate('SETTINGS');
        }
    },

    goBack() {
        if (!canNavigate()) return false;

        // Special case for settings screen
        if (state.currentScreen === 'SETTINGS') {
            if (state.beforeSettingsScreen) {
                this.navigate(state.beforeSettingsScreen);
                state.beforeSettingsScreen = null;
                return true;
            } else if (state.isMainMenuActive) {
                // If we were at main menu before settings, go back to main menu
                this.navigate('MAIN_MENU');
                return true;
            }
        }

        // If we're at a nested screen
        if (state.navigationStack.length > 1) {
            // Remove current screen from stack
            state.navigationStack.pop();

            // Go to parent screen
            const parentScreen = state.navigationStack[state.navigationStack.length - 1];

            if (parentScreen && screens[parentScreen]) {
                state.animationDirection = 'backward';

                // Add to history if not already there
                if (!state.history.includes(parentScreen)) {
                    state.history.push(parentScreen);
                }

                // Navigate to parent
                state.isNavigating = true;
                state.previousScreen = state.currentScreen;
                state.currentScreen = parentScreen;
                state.menuIsVisible = false;

                // Update UI elements
                updateGlobalElements(screens[parentScreen]);

                // Reset navigation flag after a delay
                setTimeout(() => {
                    state.isNavigating = false;
                }, ANIMATION_DURATION + ANIMATION_BUFFER);

                return true;
            }
        }

        // If we have history but not nested
        if (state.history.length > 1) {
            state.history.pop(); // Remove current screen
            const previousScreen = state.history[state.history.length - 1];

            if (previousScreen && screens[previousScreen]) {
                state.animationDirection = 'backward';

                // Navigate to previous
                state.isNavigating = true;
                state.previousScreen = state.currentScreen;
                state.currentScreen = previousScreen;
                state.menuIsVisible = false;

                // Update UI elements
                updateGlobalElements(screens[previousScreen]);

                // Reset navigation flag after a delay
                setTimeout(() => {
                    state.isNavigating = false;
                }, ANIMATION_DURATION + ANIMATION_BUFFER);

                return true;
            }
        }

        // If we're at the top level, go to main menu (null state)
        if (state.currentScreen) {
            // Animate to main menu
            animateToFrom(state.currentScreen, 'MAIN_MENU');

            // Clear history
            state.history = [];
            state.navigationStack = [];

            return true;
        }

        // No history and already at main menu, close menu
        this.closeMenu();
        return false;
    },

    // Method to directly go to main menu and clear all history
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
            return true; // MAIN_MENU is implicitly in all paths
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

    // For debugging
    debug() {
        return {
            currentScreen: state.currentScreen,
            previousScreen: state.previousScreen,
            history: [...state.history],
            navigationStack: [...state.navigationStack],
            isMainMenuActive: state.isMainMenuActive,
            beforeSettingsScreen: state.beforeSettingsScreen,
            isFirstNavigation: state.isFirstNavigation,
            animationDirection: state.animationDirection,
            isAnimating: state.isAnimating,
            menuIsVisible: state.menuIsVisible,
            targetScreen: state.targetScreen
        };
    }
}