import {getIpAddress} from "@/js/static.js";

const API_URL = getIpAddress() + '/api/v1/auth/inventory';

export default class InventoryApi {

    static async get() {
        const response = await fetch(`${API_URL}`, {
            credentials: "include",
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async add(item, slot) {
        const response = await fetch(`${API_URL}`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                item: item,
                slot: slot
            })
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async remove(inventoryItem) {
        const response = await fetch(`${API_URL}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                item: inventoryItem
            })
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async move(inventoryItem, newSlot) {
        const response = await fetch(`${API_URL}`, {
            method: 'PATCH',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                item: inventoryItem,
                newSlot: newSlot
            })
        })

        return response.status === 200
    }

    static async canPlace(item, slot) {
        const response = await fetch(`${API_URL}/can-place`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                item: item,
                slot: slot
            })
        })

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }
}