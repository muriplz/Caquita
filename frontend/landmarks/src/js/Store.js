import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null,
    }),

    getters: {
        getUser: (state) => state.user,
    },

    actions: {
        setUser(id, username, creation, connection, trust, avatar) {
            this.user = new User(id, username, creation, connection, trust, avatar);
        },

        removeUser() {
            this.user = null;
        },
    },
});