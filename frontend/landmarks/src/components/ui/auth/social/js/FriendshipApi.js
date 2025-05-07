import {getIpAddress} from "@/js/Static.js";

const API_URL = getIpAddress() + '/api/v1/auth/friends';

export default class FriendshipApi {

    static async getFriends() {
        const response = await fetch(`${API_URL}`, {
            credentials: "include",
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async getAvatars() {
        const response = await fetch(`${API_URL}/avatars`, {
            credentials: "include",
        });

        if (!response.ok) return null;
        return await response.json();
    }

    static async getPendingRequests() {
        const response = await fetch(`${API_URL}/pending`, {
            credentials: "include",
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async getSentRequests() {
        const response = await fetch(`${API_URL}/sent`, {
            credentials: "include",
        });

        if (!response.ok) throw new Error(response.status);
        return await response.json();
    }

    static async sendRequest(username) {
        const response = await fetch(`${API_URL}/request`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username
            })
        })

        if (response.status === 400) {
            const error = await response.text();
            window.alert(error || 'Registration failed: Invalid request');
            return false;
        }
        return response.status === 201;
    }

    static async respondToRequest(requesterId, action) {
        const response = await fetch(`${API_URL}/respond`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                requesterId: requesterId,
                action: action
            })
        });

        if (!response.ok) throw new Error(response.status);
        return response.status === 200;
    }

    static async removeFriend(friendId) {
        const response = await fetch(`${API_URL}/${friendId}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (!response.ok) throw new Error(response.status);
        return response.status === 200;
    }

    static async blockUser(blockId) {
        const response = await fetch(`${API_URL}/block`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                blockId: blockId
            })
        });

        if (!response.ok) throw new Error(response.status);
        return response.status === 200;
    }
}