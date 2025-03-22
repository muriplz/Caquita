export default class InventoryUtil {
    // Convert a 2D shape array to absolute cell indices based on starting position
    static getCellIndicesFromShape(shape, startRow, startCol, gridWidth) {
        const cells = [];

        for (let r = 0; r < shape.length; r++) {
            for (let c = 0; c < shape[r].length; c++) {
                if (shape[r][c] === 1) {
                    const row = startRow + r;
                    const col = startCol + c;
                    const cellIndex = row * gridWidth + col;
                    cells.push(cellIndex);
                }
            }
        }

        return cells;
    }

    // Check if an item can be placed at the given position
    static canPlaceItem(item, startRow, startCol, inventory) {
        const shape = item.shape;
        const inventoryWidth = inventory.width;
        const inventoryHeight = inventory.height;

        // Check boundaries
        if (startRow < 0 || startCol < 0) return false;
        if (startRow + shape.length > inventoryHeight) return false;

        for (let r = 0; r < shape.length; r++) {
            if (startCol + shape[r].length > inventoryWidth) return false;

            for (let c = 0; c < shape[r].length; c++) {
                if (shape[r][c] === 1) {
                    const row = startRow + r;
                    const col = startCol + c;
                    const cellIndex = row * inventoryWidth + col;

                    // Check if cell is already occupied
                    if (inventory.items.some(invItem => invItem.cells.includes(cellIndex))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // Find a valid position to place an item
    static findValidPosition(item, inventory) {
        const shape = item.shape;

        for (let r = 0; r <= inventory.height - shape.length; r++) {
            for (let c = 0; c <= inventory.width - shape[0].length; c++) {
                if (this.canPlaceItem(item, r, c, inventory)) {
                    return { row: r, col: c };
                }
            }
        }

        return null; // No valid position found
    }

    static getActualDimensions(shape) {
        if (!shape || shape.length === 0) return {width: 1, height: 1};

        let minRow = shape.length;
        let maxRow = 0;
        let minCol = shape[0].length;
        let maxCol = 0;

        for (let r = 0; r < shape.length; r++) {
            for (let c = 0; c < shape[r].length; c++) {
                if (shape[r][c] === 1) {
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }

        const height = maxRow - minRow + 1;
        const width = maxCol - minCol + 1;

        return {width, height, minRow, minCol};
    }
}