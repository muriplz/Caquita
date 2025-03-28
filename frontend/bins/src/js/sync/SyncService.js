// js/sync/SyncService.js
import SyncClient from './SyncClient.js';

let syncClient = null;

const SyncService = {
    init() {
        if (!syncClient) {
            syncClient = new SyncClient();
            syncClient.connect();
        }
        return syncClient;
    },

    getClient() {
        if (!syncClient) {
            return this.init();
        }
        return syncClient;
    },

    disconnect() {
        if (syncClient) {
            syncClient.disconnect();
            syncClient = null;
        }
    }
};

export default SyncService;