export default class InventoryItem {
    constructor(instanceId, itemId, name, width, height, imageUrl = null, rarity = 'COMMON') {
        this.instanceId = instanceId;
        this.itemId = itemId;
        this.name = name || this.formatItemName(itemId);
        this.width = width || 1;
        this.height = height || 1;
        this.imageUrl = imageUrl;
        this.rarity = rarity;
        this.properties = {};
    }

    formatItemName(itemId) {
        if (!itemId) return "Item";
        return itemId.split(':').pop()
            .split('_')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ');
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
            instanceId: this.instanceId,
            itemId: this.itemId,
            name: this.name,
            width: this.width,
            height: this.height,
            imageUrl: this.imageUrl,
            rarity: this.rarity,
            properties: {...this.properties}
        };
    }

    static fromJSON(json) {
        const item = new InventoryItem(
            json.instanceId,
            json.itemId || json.id,
            json.name,
            json.width || 1,
            json.height || 1,
            json.imageUrl,
            json.rarity
        );

        if (json.properties) {
            item.properties = {...json.properties};
        }

        return item;
    }
}