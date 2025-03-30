import SyncClient from './SyncClient.js';

let syncClient = null;

const SyncService = {
    init() {
        if (!syncClient) {
            syncClient = new SyncClient();
            return syncClient.connect();
        }
        return Promise.resolve();
    },

    getClient() {
        if (!syncClient) {
            return this.init().then(() => syncClient);
        }
        return Promise.resolve(syncClient);
    },

    disconnect() {
        if (syncClient) {
            syncClient.disconnect();
            syncClient = null;
        }
    }
};

export default SyncService;