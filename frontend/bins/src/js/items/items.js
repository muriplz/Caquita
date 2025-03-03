import {getIpAddress} from "@/js/static.js";

const API_URL = getIpAddress() + '/api/v1/items';

// Get all items
async function getAllItems() {
    const response = await fetch(`${API_URL}`);
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }
    return await response.json();
}

// Get a specific item by ID
async function getItemById(id) {
    const response = await fetch(`${API_URL}/${id}`);
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }
    return await response.json();
}

export { getAllItems, getItemById };