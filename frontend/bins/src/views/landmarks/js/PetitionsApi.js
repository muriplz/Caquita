import {getIpAddress} from "@/js/Static.js";

const API_URL = getIpAddress() + '/api/v1/petitions';

export default class PetitionsApi {

    static async get(page, orderBy) {
        const response = await fetch(`${API_URL}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                body: JSON.stringify({
                    page: page,
                    orderBy: orderBy
                })
            }
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static getName(id) {
        const language = 'en_us';
        const key = "items." + id.replace(":", ".");

        return import(`@/i18n/${language}.json`)
            .then(translations => translations[key] || id);
    }

    static async create(petition) {
        const response = await fetch(`${API_URL}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(petition)
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }


}