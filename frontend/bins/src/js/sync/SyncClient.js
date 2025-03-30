import {isProduction} from "@/js/Static.js";

export default class SyncClient {
    constructor() {
        this.socket = null;
        this.connected = false;
        this.reconnectTimeout = null;
        this.reconnectInterval = 3000;
        this.connectionPromise = null;
        this.connectionResolve = null;
    }

    connect() {
        if (this.connected) {
            return Promise.resolve();
        }

        this.connectionPromise = new Promise(resolve => {
            this.connectionResolve = resolve;
        });

        const wsUrl = isProduction() ?
            "wss://caquita.app/api/sync"
            : "ws://localhost:6996/api/sync";

        try {
            this.socket = new WebSocket(wsUrl);

            this.socket.onopen = () => {
                this.connected = true;

                // Auto-subscribe to currencies
                this.subscribe('currencies');

                // Resolve the connection promise
                if (this.connectionResolve) {
                    this.connectionResolve();
                }
            };

            this.socket.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);

                    if (message.type === 'UPDATE' && message.entity === 'currencies') {
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

        return this.connectionPromise;
    }

    subscribe(entity) {
        if (this.connected) {
            const message = JSON.stringify({
                type: 'SUBSCRIBE',
                entity
            });
            this.socket.send(message);
            return true;
        }
        return false;
    }

    unsubscribe(entity) {
        if (this.connected) {
            this.socket.send(JSON.stringify({
                type: 'UNSUBSCRIBE',
                entity
            }));
            return true;
        }
        return false;
    }

    disconnect() {
        if (this.socket) {
            clearTimeout(this.reconnectTimeout);
            this.socket.close();
            this.connected = false;
        }
    }
}