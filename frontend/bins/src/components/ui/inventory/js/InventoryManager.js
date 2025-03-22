import InventoryUtils from './InventoryUtils.js';

export default class InventoryManager {
    constructor(store) {
        this.store = store;
    }

    updateItemPosition(item, newSlot, gridWidth) {
        const inventory = this.store.getInventory();
        if (!inventory || !inventory.items) return false;

        // Find the item in the inventory
        const inventoryItem = inventory.items.find(invItem =>
            invItem.cells && invItem.cells.includes(item.inventoryItemId));

        if (!inventoryItem) return false;

        // Calculate new row/col based on the slot
        const newRow = Math.floor(newSlot / gridWidth);
        const newCol = newSlot % gridWidth;

        // Get the current position
        const oldRow = Math.floor(item.inventoryItemId / gridWidth);
        const oldCol = item.inventoryItemId % gridWidth;

        // Calculate the offset
        const rowOffset = newRow - oldRow;
        const colOffset = newCol - oldCol;

        // Update the cells with the new positions
        const newCells = item.cells.map(cellIndex => {
            const cellRow = Math.floor(cellIndex / gridWidth);
            const cellCol = cellIndex % gridWidth;
            return (cellRow + rowOffset) * gridWidth + (cellCol + colOffset);
        });

        // Update the inventory item
        inventoryItem.cells = newCells;

        // Replace the item in the inventory
        const itemIndex = inventory.items.findIndex(invItem =>
            invItem.cells && invItem.cells.includes(item.inventoryItemId));

        if (itemIndex !== -1) {
            inventory.items[itemIndex] = inventoryItem;
        }

        return true;
    }

    // Process inventory data for UI consumption
    processInventoryItems(inventory, allItems) {
        const processedItems = [];
        const occupiedCells = {};

        if (inventory.items && inventory.items.length > 0) {
            inventory.items.forEach(inventoryItem => {
                const item = allItems.find(i => i.id === inventoryItem.id);
                if (!item) return;

                const firstCellIndex = inventoryItem.cells[0];
                const baseRow = Math.floor(firstCellIndex / inventory.width);
                const baseCol = firstCellIndex % inventory.width;

                // Mark cells as occupied
                inventoryItem.cells.forEach(cellIndex => {
                    occupiedCells[cellIndex] = {
                        id: item.id,
                        inventoryItemId: firstCellIndex
                    };
                });

                processedItems.push({
                    id: item.id,
                    inventoryItemId: firstCellIndex,
                    row: baseRow,
                    col: baseCol,
                    shape: item.shape,
                    cells: inventoryItem.cells,
                    rarity: item.rarity
                });
            });
        }

        return { processedItems, occupiedCells };
    }
}