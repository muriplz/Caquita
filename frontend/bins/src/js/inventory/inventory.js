import {getIpAddress} from "@/js/static.js";

const API_URL = getIpAddress() + '/api/v1/auth/inventory';

// Get user's inventory
async function getInventory() {
    try {
        const response = await fetch(`${API_URL}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error fetching inventory:', error);
        throw error;
    }
}

// Add an item to the inventory
async function addItem(item, position) {
    const response = await fetch(`${API_URL}/add`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            item: item,
            position: position
        })
    });

    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
}

// Remove an item from the inventory
async function removeItem(itemId, position) {
    const response = await fetch(`${API_URL}/remove`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            itemId: itemId,
            position: position
        })
    });

    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
}

// Move an item in the inventory
async function moveItem(itemId, currentPosition, newPosition) {
    const response = await fetch(`${API_URL}/move`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            itemId: itemId,
            currentPosition: currentPosition,
            newPosition: newPosition
        })
    });

    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
}

// Check if an item can be placed at a position
async function canPlaceItem(item, position, itemId = null) {
    const payload = {
        position: position
    };

    if (itemId) {
        payload.itemId = itemId;
    } else if (item) {
        payload.item = item;
    }

    const response = await fetch(`${API_URL}/can-place`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const result = await response.json();
    return result.canPlace;
}

export {
    getInventory,
    addItem,
    removeItem,
    moveItem,
    canPlaceItem
};