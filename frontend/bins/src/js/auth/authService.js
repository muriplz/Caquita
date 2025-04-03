import Store from "../Store.js";
import {getIpAddress} from "../Static.js";
import router from "@/router/index.js";

class AuthService {
    async getUsername(id) {
        const response = await fetch(getIpAddress() + `/api/v1/auth?id=${id}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 200) {
            return await response.json();
        }
    }
    async login(username, password) {
        const response = await fetch(getIpAddress() + '/api/v1/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (response.status === 200) {
            const { token, id, username, creation, connection, trust } = await response.json();
            this.saveToken(token);
            await Store.setUser(id, username, creation, connection, trust);

            return response;
        } else if (response.status === 460) {
            return null;
        } else if (response.status === 400) {
            const error = await response.text();

            window.alert(error || 'Login failed: Invalid request');
            return false;
        } else {
            return false;
        }
    }

    async register(username, password) {
        const response = await fetch(getIpAddress() + '/api/v1/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (response.status === 201) {
            await this.login(username, password);
            return true;
        } else if (response.status === 400) {
            const error = await response.text();
            window.alert(error || 'Registration failed: Invalid request');
            return false;
        } else {
            return false;
        }
    }

    async validate() {
        if (!this.getToken()) {
            return false;
        }

        const response = await fetch(getIpAddress() + '/api/v1/auth/validate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
        });

        if (response.status === 200) {
            const { id, username, creation, connection, trust } = await response.json()
            await Store.setUser(id, username, creation, connection, trust)

            return true;
        } else {
            return false;
        }
    }

    saveToken(token) {
        document.cookie = `auth=${token}; path=/;`;
    }

    getToken() {
        const cookies = document.cookie.split('; ');
        for (let i = 0; i < cookies.length; i++) {
            const cookie = cookies[i];
            const [name, value] = cookie.split('=');
            if (name === 'auth') {
                return value;
            }
        }
        return null;
    }

    logout() {
        Store.removeUser();
        router.push('/');
        document.cookie = 'auth=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }
}

export default new AuthService();