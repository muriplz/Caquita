import {getIpAddress} from "../static.js";
import * as THREE from 'three';
import settingsManager from '@/components/ui/settings/settings.js';

export class TileLoader {
    constructor() {
        this.baseUrl = getIpAddress() + '/api/v1/map/tiles';
        this.textureLoader = new THREE.TextureLoader();
        this.cache = new Map();
        this.baseCacheSize = 200;
        this.lowPriQueue = [];
        this.highPriQueue = [];
        this.isLoading = false;
        this.maxConcurrentLoads = 4;
    }

    /**
     * Calculate dynamic cache size based on render distance
     */
    get maxCacheSize() {
        const renderDistance = settingsManager.settings.graphics.renderDistance;
        // Calculate tiles in view (square of (2*renderDistance+1))
        const tilesInView = Math.pow((2 * renderDistance + 1), 2);
        // Add buffer for movement (4x current view is usually sufficient)
        return Math.max(this.baseCacheSize, tilesInView * 4);
    }

    /**
     * Load a map tile with options
     * @param {number} zoom - Zoom level
     * @param {number} x - Tile X coordinate
     * @param {number} y - Tile Y coordinate
     * @param {Object} options - Options
     * @returns {Promise<THREE.Texture>}
     */
    loadTile(zoom, x, y, options = {}) {
        const priority = options.priority || 'normal';
        const cacheKey = `${zoom}/${x}/${y}`;

        // Return cached tile if available
        if (this.cache.has(cacheKey)) {
            return Promise.resolve(this.cache.get(cacheKey));
        }

        // Create placeholder texture
        const canvas = document.createElement('canvas');
        canvas.width = 256;
        canvas.height = 256;
        const ctx = canvas.getContext('2d');

        ctx.fillRect(0, 0, 256, 256);

        // Create initial texture from canvas
        const texture = new THREE.CanvasTexture(canvas);
        texture.needsUpdate = true;

        // Store in cache immediately (with placeholder)
        this.cache.set(cacheKey, texture);
        this.cleanCache();

        // Queue for loading
        const loadRequest = {
            zoom, x, y,
            texture,
            canvas,
            cacheKey,
            resolve: null,
            reject: null
        };

        const promise = new Promise((resolve, reject) => {
            loadRequest.resolve = resolve;
            loadRequest.reject = reject;
        });

        // Add to appropriate queue
        if (priority === 'high') {
            this.highPriQueue.push(loadRequest);
        } else {
            this.lowPriQueue.push(loadRequest);
        }

        // Start processing if not already
        this.processQueue();

        // Return the placeholder texture that will update later
        return promise;
    }

    processQueue() {
        if (this.isLoading) return;

        const activeLoads = [];

        // Process high priority queue first
        while (activeLoads.length < this.maxConcurrentLoads && this.highPriQueue.length > 0) {
            const request = this.highPriQueue.shift();
            activeLoads.push(this.loadTileData(request));
        }

        // Then process low priority queue
        while (activeLoads.length < this.maxConcurrentLoads && this.lowPriQueue.length > 0) {
            const request = this.lowPriQueue.shift();
            activeLoads.push(this.loadTileData(request));
        }

        if (activeLoads.length > 0) {
            this.isLoading = true;
            Promise.all(activeLoads).finally(() => {
                this.isLoading = false;
                if (this.highPriQueue.length > 0 || this.lowPriQueue.length > 0) {
                    // Continue processing if more items in queue
                    setTimeout(() => this.processQueue(), 50);
                }
            });
        }
    }

    loadTileData(request) {
        const { zoom, x, y, texture, canvas, cacheKey, resolve, reject } = request;

        // Construct URL
        let url = `${this.baseUrl}/${zoom}/${x}/${y}`;

        return new Promise((resolveLoad) => {
            try {
                const image = new Image();
                image.crossOrigin = 'anonymous';

                image.onload = () => {
                    const ctx = canvas.getContext('2d');
                    canvas.width = image.width;
                    canvas.height = image.height;
                    ctx.drawImage(image, 0, 0);

                    // Update the texture
                    texture.needsUpdate = true;
                    resolve(texture);
                    resolveLoad();
                };

                image.onerror = (err) => {
                    console.error(`Failed to load tile image ${cacheKey}:`, err);
                    // Still resolve with placeholder
                    resolve(texture);
                    resolveLoad();
                };

                image.src = url;

            } catch (error) {
                console.error(`Error in loadTile ${cacheKey}:`, error);
                reject(error);
                resolveLoad();
            }
        });
    }

    cleanCache() {
        const currentMaxSize = this.maxCacheSize;
        if (this.cache.size <= currentMaxSize) return;

        // Remove oldest 20% of entries
        const keysToDelete = [...this.cache.keys()]
            .slice(0, Math.floor(this.cache.size * 0.2));

        keysToDelete.forEach(key => this.cache.delete(key));
    }
}

export const tileLoader = new TileLoader();