export default class Position {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    equals(position) {
        return this.x === position.x && this.y === position.y;
    }

    toString() {
        return `${this.x},${this.y}`;
    }

    static fromString(str) {
        const [x, y] = str.split(',').map(Number);
        return new Position(x, y);
    }
}