// Utility to load item names from JSON and generate image paths
export default {
    itemNames: null,
    loading: false,
    initialized: false,

    async init() {
        if (this.loading || this.initialized) return;

        this.loading = true;
        try {
            const response = await fetch('/i18n/items/en_us.json');
            if (!response.ok) {
                throw new Error(`Failed to load translations: ${response.status}`);
            }
            this.itemNames = await response.json();
            this.initialized = true;
        } catch (error) {
            console.error('Error loading item translations:', error);
            this.itemNames = {}; // Empty object as fallback
        } finally {
            this.loading = false;
        }
    },

    async getItemName(itemId) {
        if (!this.initialized) {
            await this.init();
        }

        if (!itemId) return "Item";

        // Convert item ID format (e.g., "glass:bottle") to the JSON key format (e.g., "items.glass.bottle")
        const key = "items." + itemId.replace(':', '.');

        // Use the name from JSON if available, otherwise fallback to the original formatting
        if (this.itemNames && this.itemNames[key]) {
            return this.itemNames[key];
        }

        return itemId.split(':').pop()
            .split('_')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ');
    },

    getItemImagePath(itemId) {
        if (!itemId) return null;

        // Convert itemId format (e.g., "glass:bottle") to path format (e.g., "glass/bottle")
        const path = itemId.replace(':', '/');

        return `/images/items/${path}.png`;
    }
}