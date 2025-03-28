// js/sync/SyncClient.js
import {getIpAddress} from "@/js/Static.js";

export default class SyncClient {
    constructor() {
        this.socket = null;
        this.connected = false;
        this.reconnectTimeout = null;
        this.reconnectInterval = 3000;
    }

    connect() {
        if (this.connected) {
            return;
        }

        const wsUrl = "ws://localhost:6996/api/sync";

        try {
            this.socket = new WebSocket(wsUrl);

            this.socket.onopen = () => {
                this.connected = true;

                // Auto-subscribe to currencies
                this.subscribe('currencies');
            };

            this.socket.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);

                    if (message.type === 'UPDATE' && message.entity === 'currencies') {

                        // Import dynamically to avoid circular reference
                        import('./SyncStore').then(module => {
                            const SyncStore = module.default;
                            SyncStore.updateCurrencies(message.data);
                        });
                    }
                } catch (e) {
                }
            };

            this.socket.onclose = (event) => {
                this.connected = false;
                this.reconnectTimeout = setTimeout(() => this.connect(), this.reconnectInterval);
            };

        } catch (e) {
        }
    }

    subscribe(entity) {
        if (this.connected) {
            const message = JSON.stringify({
                type: 'SUBSCRIBE',
                entity
            });
            this.socket.send(message);
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