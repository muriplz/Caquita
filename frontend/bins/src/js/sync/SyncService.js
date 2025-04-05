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

    subscribe(entity, callback) {
        return this.getClient().then(client => {
            return client.onMessage(entity, callback);
        });
    },

    sendLocationUpdate(latitude, longitude, radius) {
        return this.getClient().then(client => {
            return client.sendLocationData(latitude, longitude, radius);
        });
    },

    disconnect() {
        if (syncClient) {
            syncClient.disconnect();
            syncClient = null;
        }
    }
};

export default SyncService;