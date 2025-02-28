import User from './user.js';

class Store {
    constructor() {
        this.state = {
            user: null
        };
    }

    setUser(username, creation, trust) {
        this.state.user = new User(username, creation, trust);
    }

    removeUser() {
        this.state.user = null;
    }

    getUser() {
        return this.state.user;
    }
}

export default new Store();