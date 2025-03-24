import {getIpAddress} from "@/js/static.js";

const API_URL = getIpAddress() + '/api/v1/items';

export default class ItemsApi {

    static async get() {
        const response = await fetch(`${API_URL}`, {});

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static getName(id) {
        const language = 'en_us';
        const key = "items." + id.replace(":", ".");

        return import(`@/i18n/${language}.json`)
            .then(translations => translations[key] || id);
    }


}