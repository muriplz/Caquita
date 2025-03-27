// Levels.js
import {getIpAddress} from "@/js/Static.js";
import Store from "@/js/Store.js";
import Level from "@/js/auth/Level.js";

const API_URL = getIpAddress() + '/api/v1/auth/level';

class Levels {
    static async getLevel(userId) {
        if (!userId) {
            console.error('User ID is undefined');
            throw new Error('User ID is required');
        }

        const response = await fetch(`${API_URL}/${userId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        return await response.json();
    }

    static async modifyLevel(amount) {
        const response = await fetch(`${API_URL}`, {
            method: 'PATCH',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                amount: amount
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        return await response.json();
    }

    static async setup() {
        try {
            const user = Store.getUser();
            if (!user || !user.id) {
                console.error('Cannot set up level: User ID is missing');
                return;
            }

            const levelData = await this.getLevel(user.id);
            const levelObj = new Level(levelData);
            Store.setLevel(levelObj);
        } catch (error) {
            console.error('Error setting up level:', error);
        }
    }
}

export default Levels;