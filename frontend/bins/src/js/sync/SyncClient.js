import { isProduction } from "@/js/Static.js";

export default class SyncClient {
    constructor() {
        this.socket = null;
        this.connected = false;
        this.reconnectTimeout = null;
        this.reconnectInterval = 3000;
        this.maxReconnectAttempts = 10;
        this.reconnectAttempts = 0;
        this.connectionPromise = null;
        this.connectionResolve = null;
        this.lastHeartbeatResponse = Date.now();
        this.heartbeatInterval = null;
        this.subscriptions = new Set();
        this.messageHandlers = new Map();
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
                this.reconnectAttempts = 0;
                this.startHeartbeat();

                // Resubscribe to all previous subscriptions
                this.resubscribe();

                // Resolve the connection promise
                if (this.connectionResolve) {
                    this.connectionResolve();
                }
            };

            this.socket.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    console.log("Received WebSocket message:", message.type, message.entity);

                    if (message.type === 'CONNECTED') {
                        console.log('Connected to sync server');
                    } else if (message.type === 'HEARTBEAT') {
                        this.send({ type: 'HEARTBEAT' });
                    } else if (message.type === 'HEARTBEAT_ACK') {
                        this.lastHeartbeatResponse = Date.now();
                    } else if (message.type === 'UPDATE') {
                        const handlers = this.messageHandlers.get(message.entity);
                        if (handlers) {
                            handlers.forEach(handler => handler(message.data));
                        }
                    } else if (message.type === 'ERROR') {
                        console.error('Sync error:', message.data);
                    }
                } catch (e) {
                    console.error('Error processing message:', e);
                }
            };

            this.socket.onclose = (event) => {
                this.connected = false;
                this.stopHeartbeat();

                if (this.reconnectAttempts < this.maxReconnectAttempts) {
                    this.reconnectTimeout = setTimeout(() => this.connect(), this.getReconnectDelay());
                } else {
                    console.error('Max reconnect attempts reached');
                }
            };

            this.socket.onerror = (error) => {
                console.error('WebSocket error:', error);
            };

        } catch (e) {
            console.error('Error establishing WebSocket connection:', e);
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
                this.reconnectTimeout = setTimeout(() => this.connect(), this.getReconnectDelay());
            }
        }

        return this.connectionPromise;
    }

    getReconnectDelay() {
        const delay = Math.min(30000, this.reconnectInterval * Math.pow(1.5, this.reconnectAttempts));
        this.reconnectAttempts++;
        return delay;
    }

    startHeartbeat() {
        this.heartbeatInterval = setInterval(() => {
            if (this.connected) {
                if (Date.now() - this.lastHeartbeatResponse > 60000) {
                    console.log('No heartbeat response received, reconnecting...');
                    this.socket.close();
                    return;
                }

                this.send({ type: 'HEARTBEAT' });
            }
        }, 30000);
    }

    stopHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
            this.heartbeatInterval = null;
        }
    }

    resubscribe() {
        for (const entity of this.subscriptions) {
            this.subscribe(entity);
        }
    }

    send(message) {
        if (this.connected && this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify(message));
            return true;
        }
        return false;
    }

    subscribe(entity) {
        this.subscriptions.add(entity);
        return this.send({
            type: 'SUBSCRIBE',
            entity
        });
    }

    unsubscribe(entity) {
        this.subscriptions.delete(entity);
        return this.send({
            type: 'UNSUBSCRIBE',
            entity
        });
    }

    sendLocationData(latitude, longitude, radius) {
        return this.send({
            type: 'LOCATION_DATA',
            data: { latitude, longitude, radius }
        });
    }

    onMessage(entity, handler) {
        if (!this.messageHandlers.has(entity)) {
            this.messageHandlers.set(entity, new Set());
        }
        this.messageHandlers.get(entity).add(handler);

        // Auto-subscribe if connected
        if (!this.subscriptions.has(entity)) {
            this.subscribe(entity);
        }

        // Return unsubscribe function
        return () => {
            if (this.messageHandlers.has(entity)) {
                const handlers = this.messageHandlers.get(entity);
                handlers.delete(handler);
                if (handlers.size === 0) {
                    this.messageHandlers.delete(entity);
                    this.unsubscribe(entity);
                }
            }
        };
    }

    disconnect() {
        if (this.socket) {
            clearTimeout(this.reconnectTimeout);
            this.stopHeartbeat();
            this.socket.close();
            this.connected = false;
            this.socket = null;
        }
    }
}