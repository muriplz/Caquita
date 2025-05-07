import {getIpAddress} from "@/js/Static.js";

const API_URL = getIpAddress() + '/api/v1/auth/avatars';

export default class AvatarsApi {

    static async getUnlocked() {
        const response = await fetch(`${API_URL}`, {
            method: 'GET',
            credentials: 'include',
        });

        if (!response.ok) return null;
        return await response.json();
    }

    static async changeAvatar(avatar) {
        const response = await fetch(`${API_URL}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({
                avatar: avatar
            })
        });

        return response.status === 201
    }

}