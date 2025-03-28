// js/sync/SyncClient.js
import {getIpAddress} from "@/js/Static.js";

export default class SyncClient {
    constructor() {
        this.socket = null;
        this.connected = false;
        this.reconnectTimeout = null;
        this.reconnectInterval = 3000;
        console.log("SyncClient constructor called");
    }

    connect() {
        console.log("SyncClient connect() called");
        if (this.connected) {
            console.log("Already connected, skipping");
            return;
        }

        const wsUrl = "ws://localhost:6996/api/sync";
        console.log(`Attempting WebSocket connection to: ${wsUrl}`);

        try {
            this.socket = new WebSocket(wsUrl);

            this.socket.onopen = () => {
                this.connected = true;
                console.log('✅ Sync connected successfully');

                // Auto-subscribe to currencies
                this.subscribe('currencies');
            };

            this.socket.onmessage = (event) => {
                console.log('📩 Received sync message:', event.data);
                try {
                    const message = JSON.parse(event.data);
                    console.log('Parsed message:', message);

                    if (message.type === 'UPDATE' && message.entity === 'currencies') {
                        console.log('Currencies data received:', message.data);

                        // Import dynamically to avoid circular reference
                        const SyncStore = require('./SyncStore').default;
                        SyncStore.updateCurrencies(message.data);
                    }
                } catch (e) {
                    console.error('Error processing message:', e);
                }
            };

            this.socket.onclose = (event) => {
                this.connected = false;
                console.log(`WebSocket closed with code: ${event.code}, reason: ${event.reason}`);
                this.reconnectTimeout = setTimeout(() => this.connect(), this.reconnectInterval);
            };

            this.socket.onerror = (error) => {
                console.error('❌ WebSocket error:', error);
            };

        } catch (e) {
            console.error('Error creating WebSocket:', e);
        }
    }

    subscribe(entity) {
        console.log(`Attempting to subscribe to ${entity}`);
        if (this.connected) {
            const message = JSON.stringify({
                type: 'SUBSCRIBE',
                entity
            });
            console.log(`Sending subscription: ${message}`);
            this.socket.send(message);
        } else {
            console.warn(`Cannot subscribe to ${entity} - not connected`);
        }
    }

    unsubscribe(entity) {
        if (this.connected) {
            this.socket.send(JSON.stringify({
                type: 'UNSUBSCRIBE',
                entity
            }));
        }
    }

    disconnect() {
        if (this.socket) {
            clearTimeout(this.reconnectTimeout);
            this.socket.close();
            this.connected = false;
        }
    }
}