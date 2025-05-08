import {getIpAddress} from "@/js/Static.js";

const API_URL = getIpAddress() + '/api/v1/petitions'

export default class PetitionsApi {

    static async get(page, orderBy, status) {
        const response = await fetch(`${API_URL}?page=${page}&orderBy=${orderBy}&status=${status}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        })

        if (!response.ok) throw new Error(response.status)
        return await response.json()
    }

    static async byId(id) {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        })

        if (!response.ok) throw new Error(response.status)
        return await response.json()
    }

    static getName(id) {
        const language = 'en_us'
        const key = "items." + id.replace(":", ".")

        return import(`@/i18n/${language}.json`)
            .then(translations => translations[key] || id)
    }

    static async create(description, type, lat, lon, landmarkInfo) {
        const response = await fetch(`${API_URL}`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(
                {
                    description: description,
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

    static async replies(id) {
        const response = await fetch(`${API_URL}/${id}/replies`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async sendMessage(id, message) {
        const response = await fetch(`${API_URL}/${id}/send-message `, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: message
            })
        });

        if (response.status === 201) {
            return true;
        } else if (response.status === 400) {
            const error = await response.text();

            window.alert(error || 'Login failed: Invalid request');
            return false;
        } else {
            return false;
        }
    }

    static async sendReply(id, message) {
        const response = await fetch(`${API_URL}/${id}/send-reply `, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: message
            })
        });

        if (response.status === 201) {
            return true;
        } else if (response.status === 400) {
            const error = await response.text();

            window.alert(error || 'Login failed: Invalid request');
            return false;
        } else {
            return false;
        }
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

    static async uploadImage(id, image) {
        const formData = new FormData();
        formData.append('image', image);

        const response = await fetch(`${API_URL}/${id}/image`, {
            method: 'POST',
            credentials: 'include',
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || response.status);
        }

        return await response.json();
    }

    static getImageUrl(id) {
        return `${API_URL}/${id}/image`;
    }

    static async delete(id) {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return response.status === 204;
    }

}