import {getIpAddress} from "@/js/static.js";

const API_URL = getIpAddress() + '/api/v1/auth/inventory';
let cachedInventory = null;
let fetchingPromise = null;

const fetchJSON = async (url, options = {}) => {
    const response = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers
        },
        credentials: 'include'
    });

    if (!response.ok) {
        const error = new Error(`HTTP error ${response.status}`);
        error.status = response.status;
        error.statusText = response.statusText;

        try {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                error.data = await response.json();
            } else {
                error.data = { message: 'Authentication required or invalid endpoint' };
            }
        } catch (e) {
            error.data = { message: 'Unknown error' };
        }

        throw error;
    }

    return response.json();
};

export default {
    async getInventory(force = false) {
        if (cachedInventory && !force) {
            return Promise.resolve(cachedInventory);
        }

        if (fetchingPromise) {
            return fetchingPromise;
        }

        fetchingPromise = fetchJSON(API_URL)
            .then(data => {
                // Backend returns the data directly matching our structure
                cachedInventory = data;
                fetchingPromise = null;
                return cachedInventory;
            })
            .catch(error => {
                console.error("Error fetching inventory:", error);
                fetchingPromise = null;
                throw error;
            });

        return fetchingPromise;
    },

    async addItem(item, position) {
        const requestPayload = {
            item: {
                id: item.itemId || item.id,
                width: item.width,
                height: item.height,
                rarity: item.rarity || 'COMMON'
            },
            position: {
                x: position.x,
                y: position.y
            }
        };

        const response = await fetchJSON(API_URL, {
            method: 'POST',
            body: JSON.stringify(requestPayload)
        });

        if (response.success) {
            cachedInventory = null;
        }

        return response;
    },

    async removeItem(instanceId) {
        const response = await fetchJSON(API_URL, {
            method: 'DELETE',
            body: JSON.stringify({ instanceId })
        });

        if (response.success) {
            cachedInventory = null;
        }

        return response;
    },

    async moveItem(instanceId, newPosition) {
        try {
            const requestBody = {
                instanceId,
                newPosition
            };

            const response = await fetchJSON(API_URL, {
                method: 'PATCH',
                body: JSON.stringify(requestBody)
            });

            if (response.success) {
                cachedInventory = null;
            }

            return response;
        } catch (error) {
            // If item can't be placed, the server returns 400 Bad Request
            if (error.status === 400) {
                return { success: false, canPlace: false };
            }
            throw error;
        }
    },

    async canPlaceItem(itemData, position) {
        const url = `${API_URL}/can-place`;

        const requestPayload = itemData.instanceId
            ? { instanceId: itemData.instanceId, position }
            : { itemId: itemData.itemId || itemData.id, position };

        const response = await fetchJSON(url, {
            method: 'POST',
            body: JSON.stringify(requestPayload)
        });

        return response.canPlace;
    },

    clearCache() {
        cachedInventory = null;
    }
}