import {getIpAddress} from "@/js/Static.js";

const API_URL = getIpAddress() + '/api/v1/stats';

export default class StatsApi {

    static async getOnlines() {
        const response = await fetch(`${API_URL}/onlines`, {});

        if (!response.ok) return false;
        return await response.json();
    }

    static async recordView() {
        const response = await fetch(`${API_URL}/record`, {
            method: 'POST',
        })
    }

    static async getStats() {
        const response = await fetch(`${API_URL}`, {});

        if (!response.ok) return false;
        return await response.json();
    }
}