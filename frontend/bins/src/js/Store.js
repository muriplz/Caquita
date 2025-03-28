import User from './auth/User.js';
import InventoryApi from "@/components/ui/inventory/js/InventoryApi.js";
import ItemsApi from "@/js/items/ItemsApi.js";
import {reactive} from "vue";

class Store {
    constructor() {
        this.state = reactive({
            user: null,
            inventory: null,
            items: null
        });

        this.listeners = new Set()
    }

    async setUser(id, username, creation, trust) {
        this.state.user = new User(id, username, creation, trust);
        await this.updateInventory();
    }

    async updateInventory() {
        this.state.inventory = await InventoryApi.get();
        this.notifyListeners('inventory-updated');
        return this.state.inventory;
    }

    async updateItems() {
        this.state.items = await ItemsApi.get();
        return this.state.items;
    }

    // Update an item's position without fetching the entire inventory
    updateItemPosition(inventoryItem, newCells) {
        if (!this.state.inventory || !this.state.inventory.items) {
            return false;
        }

        // Find the item in the inventory
        const index = this.state.inventory.items.findIndex(item =>
            item.id === inventoryItem.id
        );

        if (index === -1) {
            return false;
        }

        // Update the item's cells
        this.state.inventory.items[index].cells = newCells;

        // Notify listeners about the position change
        this.notifyListeners('item-position-changed');

        return true;
    }

    // Update an item's orientation without fetching the entire inventory
    updateItemOrientation(inventoryItem, newOrientation, newCells) {
        if (!this.state.inventory || !this.state.inventory.items) {
            return false;
        }

        // Find the item in the inventory
        const index = this.state.inventory.items.findIndex(item =>
            item.id === inventoryItem.id
        );

        if (index === -1) {
            return false;
        }

        // Update the item's orientation and cells
        this.state.inventory.items[index].orientation = newOrientation;
        if (newCells) {
            this.state.inventory.items[index].cells = newCells;
        }

        // Notify listeners about the orientation change
        this.notifyListeners('item-orientation-changed');

        return true;
    }

    removeUser() {
        this.state.user = null;
        this.removeInventory();
    }

    removeInventory() {
        this.state.inventory = null;
    }

    getUser() {
        return this.state.user;
    }

    getInventory() {
        return this.state.inventory;
    }

    getItems() {
        return this.state.items;
    }

    getItemById(id) {
        return this.state.items.find(item => item.id === id);
    }

    addListener(callback) {
        this.listeners.add(callback)
    }

    removeListener(callback) {
        this.listeners.delete(callback)
    }

    notifyListeners(event) {
        this.listeners.forEach(callback => callback(event))
    }
}

export default new Store();