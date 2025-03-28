// sync/index.js
import SyncClient from './SyncClient.js';

let syncClient = null;

export function initSync() {
    if (!syncClient) {
        syncClient = new SyncClient();
        // Connect to WebSocket
        syncClient.connect();
    }
    return syncClient;
}

export function getSyncClient() {
    if (!syncClient) {
        return initSync();
    }
    return syncClient;
}