import {getIpAddress} from "@/js/Static.js";

const API_URL = getIpAddress() + '/api/v1/petitions';

export default class PetitionsApi {

    static async get(page, orderBy, status) {
        const response = await fetch(`${API_URL}?page=${page}&orderBy=${orderBy}&status=${status}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async byId(id) {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        })

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static getName(id) {
        const language = 'en_us';
        const key = "items." + id.replace(":", ".");

        return import(`@/i18n/${language}.json`)
            .then(translations => translations[key] || id);
    }

    static async create(type, lat, lon, landmarkInfo) {
        const response = await fetch(`${API_URL}`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(
                {
                    type: type,
                    lat: lat,
                    lon: lon,
                    landmarkInfo: landmarkInfo
                }
            )
        });

        return response.status === 201;
    }

    static async messages(id) {
        const response = await fetch(`${API_URL}/${id}/messages`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async updateStatus(id, status) {
        const response = await fetch(`${API_URL}/${id}/status`, {
            method: 'PATCH',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({status})
        });

        return response.status === 200;
    }

}