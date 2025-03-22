import User from './auth/user.js';
import Levels from "@/js/auth/levels.js";
import InventoryApi from "@/components/ui/inventory/js/InventoryApi.js";
import ItemsApi from "@/js/items/ItemsApi.js";
import InventoryManager from "@/components/ui/inventory/js/InventoryManager.js";

class Store {
    constructor() {
        this.state = {
            user: null,
            level: null,
            inventory: null,
            items: null
        };
        this.inventoryManager = null;
    }

    async setUser(id, username, creation, trust, experience, beans) {
        this.state.user = new User(id, username, creation, trust, experience, beans);
        await this.updateInventory();
        await Levels.setup();
    }

    setLevel(level) {
        this.state.level = level;
    }

    async updateInventory() {
        this.state.inventory = await InventoryApi.get();
        if (!this.inventoryManager) {
            this.inventoryManager = new InventoryManager(this);
        }
        return this.state.inventory;
    }

    async updateItems() {
        this.state.items = await ItemsApi.get();
        return this.state.items;
    }

    // Update an item's position without fetching the entire inventory
    updateItemPosition(item, newSlot) {
        if (!this.inventoryManager) {
            this.inventoryManager = new InventoryManager(this);
        }

        const gridWidth = this.state.inventory?.width || 1;
        return this.inventoryManager.updateItemPosition(item, newSlot, gridWidth);
    }

    // Get processed inventory data for UI consumption
    getProcessedInventory() {
        if (!this.inventoryManager) {
            this.inventoryManager = new InventoryManager(this);
        }

        return this.inventoryManager.processInventoryItems(
            this.state.inventory,
            this.state.items
        );
    }

    removeUser() {
        this.state.user = null;
        this.removeLevel();
        this.removeInventory();
    }

    removeLevel() {
        this.state.level = null;
    }

    removeInventory() {
        this.state.inventory = null;
    }

    getUser() {
        return this.state.user;
    }

    getLevel() {
        return this.state.level;
    }

    getInventory() {
        return this.state.inventory;
    }

    getItems() {
        return this.state.items;
    }
}

export default new Store();