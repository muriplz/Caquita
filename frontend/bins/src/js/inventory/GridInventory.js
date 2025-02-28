class GridInventory {
    constructor(id, user, width, height, itemPositions = new Map()) {
        this.id = id;
        this.user = user;
        this.width = width;
        this.height = height;
        this.itemPositions = itemPositions;
    }
}

export default GridInventory;