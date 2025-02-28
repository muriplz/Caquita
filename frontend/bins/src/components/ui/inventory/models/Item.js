export default class Item {
    constructor(id, name, width, height, imageUrl = null) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.imageUrl = imageUrl;
        this.properties = {};
    }

    setProperty(key, value) {
        this.properties[key] = value;
        return this;
    }

    getProperty(key) {
        return this.properties[key];
    }

    toJSON() {
        return {
            id: this.id,
            name: this.name,
            width: this.width,
            height: this.height,
            imageUrl: this.imageUrl,
            properties: {...this.properties}
        };
    }

    static fromJSON(json) {
        const item = new Item(
            json.id,
            json.name,
            json.width || 1,
            json.height || 1,
            json.imageUrl
        );

        if (json.properties) {
            item.properties = {...json.properties};
        }

        return item;
    }
}