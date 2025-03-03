import User from './user.js';
import Levels from "@/js/auth/levels.js";

class Store {
    constructor() {
        this.state = {
            user: null,
            level: null
        };
    }

    async setUser(id, username, creation, trust) {
        this.state.user = new User(id, username, creation, trust);
        await Levels.setup();
    }

    setLevel(level) {
        this.state.level = level;
    }

    removeUser() {
        this.state.user = null;
    }

    removeLevel() {
        this.state.level = null;
    }

    getUser() {
        return this.state.user;
    }

    getLevel() {
        return this.state.level;
    }
}

export default new Store();