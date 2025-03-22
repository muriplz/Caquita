export default class Item {
    constructor(id, shape, rarity, resourceType, disposalOutcomes, recyclingReward) {
        this.id = id;
        this.shape = shape;
        this.rarity = rarity;
        this.resourceType = resourceType;
        this.disposalOutcomes = disposalOutcomes;
        this.recyclingReward = recyclingReward;
    }

    async getName() {
        const language = 'en_us';
        const key = "items." + this.id.replace(":", ".");

        const translations = await import(`@/i18n/${language}.json`);
        return translations[key] || this.id;
    }

}