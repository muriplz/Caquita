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
                console.log('[Inventory Service] Received inventory data:', data);
                cachedInventory = data;
                fetchingPromise = null;
                return cachedInventory;
            })
            .catch(error => {
                console.error('[Inventory Service] Error fetching inventory:', error);
                fetchingPromise = null;
                throw error;
            });

        return fetchingPromise;
    },

    async addItem(item, position) {
        console.log('[Inventory Service] Adding item:', item, 'at position:', position);

        const requestPayload = {
            item: {
                id: item.id,
                name: item.name,
                width: item.width,
                height: item.height
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
            console.log('[Inventory Service] Item added successfully');
            cachedInventory = null;
        }

        return response;
    },

    async removeItem(itemId) {
        console.log('[Inventory Service] Removing item:', itemId);

        const response = await fetchJSON(API_URL, {
            method: 'DELETE',
            body: JSON.stringify({ itemId })
        });

        if (response.success) {
            console.log('[Inventory Service] Item removed successfully');
            cachedInventory = null;
        }

        return response;
    },

    async moveItem(itemId, newPosition) {
        console.log('[Inventory Service] Moving item:', itemId, 'to position:', newPosition);

        try {
            const requestBody = {
                itemId,
                newPosition: {
                    x: newPosition.x,
                    y: newPosition.y
                }
            };

            console.log('[Inventory Service] Request payload:', JSON.stringify(requestBody));

            const response = await fetchJSON(API_URL, {
                method: 'PATCH',
                body: JSON.stringify(requestBody)
            });

            console.log('[Inventory Service] Move response:', response);

            if (response.success) {
                console.log('[Inventory Service] Item moved successfully');
                cachedInventory = null;
            }

            return response;
        } catch (error) {
            console.error('[Inventory Service] Error moving item:', error);
            // If item can't be placed, the server returns 400 Bad Request
            if (error.status === 400) {
                return { success: false, canPlace: false };
            }
            throw error;
        }
    },

    async canPlaceItem(itemData, position) {
        const url = `${API_URL}/can-place`;
        console.log('[Inventory Service] Checking if can place item at position:', position);

        const requestPayload = itemData.id
            ? { itemId: itemData.id, position }
            : { item: itemData, position };

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