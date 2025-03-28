// js/sync/SyncService.js
import SyncClient from './SyncClient.js';

let syncClient = null;

const SyncService = {
    init() {
        console.log("SyncService.init() called");
        if (!syncClient) {
            console.log("Creating new SyncClient");
            syncClient = new SyncClient();
            syncClient.connect();
            console.log('Sync system initialized with client:', syncClient);
        } else {
            console.log("SyncClient already exists:", syncClient);
        }
        return syncClient;
    },

    getClient() {
        console.log("SyncService.getClient() called, syncClient =", syncClient);
        if (!syncClient) {
            console.log("No existing client, initializing");
            return this.init();
        }
        return syncClient;
    },

    disconnect() {
        console.log("SyncService.disconnect() called");
        if (syncClient) {
            syncClient.disconnect();
            syncClient = null;
        }
    }
};

export default SyncService;