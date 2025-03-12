import maplibregl from 'maplibre-gl';
import { getCoordinates } from './coordinates.js';
import { DEFAULT_ZOOM } from './TileConversion.js';

class MapLibreManager {
    constructor() {
        this.map = null;
        this.container = null;
        this.styleUrl = '/jsons/map_styles/caquita.json';
        this.initialized = false;
        this.maxZoom = 20;
        this.minZoom = 0;
        this.lastCenter = null;
        this.pendingOperations = [];
        this.canvas = null;
    }

    async initialize(container) {
        if (this.initialized) return;

        this.container = container;
        const coordinates = getCoordinates();

        try {
            // Cache the style to avoid repeated fetching
            if (!this.cachedStyle) {
                const styleResponse = await fetch(this.styleUrl);
                this.cachedStyle = await styleResponse.json();
            }

            const style = this.cachedStyle;

            if (style.sources && style.sources.openmaptiles) {
                style.sources.openmaptiles = {
                    type: 'vector',
                    url: 'https://tiles-eu.stadiamaps.com/data/openmaptiles.json',
                    maxzoom: 14
                };
            }

            // Optimize map performance settings
            this.map = new maplibregl.Map({
                container: this.container,
                style: style,
                center: [coordinates.lon, coordinates.lat],
                zoom: DEFAULT_ZOOM,
                attributionControl: false,
                preserveDrawingBuffer: true,
                maxZoom: this.maxZoom,
                minZoom: this.minZoom,
                fadeDuration: 0,
                renderWorldCopies: false,
                antialias: false
            });

            await new Promise(resolve => {
                this.map.on('load', () => {
                    // Store canvas for faster access
                    this.canvas = this.map.getCanvas();

                    // Apply any pending operations
                    this.pendingOperations.forEach(op => op());
                    this.pendingOperations = [];

                    // Apply performance optimizations to canvas
                    this.optimizeCanvas();

                    resolve();
                });
            });

            this.initialized = true;
            return this.map;
        } catch (error) {
            console.error('Error initializing MapLibre:', error);
            throw error;
        }
    }

    optimizeCanvas() {
        if (!this.canvas) return;

        // Set will-change to improve graphics performance
        this.canvas.style.willChange = 'transform';

        // Disable image smoothing for better performance and sharper pixels
        const ctx = this.canvas.getContext('2d');
        if (ctx) {
            ctx.imageSmoothingEnabled = false;
        }
    }

    setCenter(lat, lon) {
        if (!this.map) {
            this.pendingOperations.push(() => this.setCenter(lat, lon));
            return;
        }

        // Skip redundant updates
        if (this.lastCenter &&
            Math.abs(this.lastCenter[0] - lat) < 0.00001 &&
            Math.abs(this.lastCenter[1] - lon) < 0.00001) {
            return;
        }

        this.lastCenter = [lat, lon];
        this.map.setCenter([lon, lat]);
    }

    setZoom(zoom) {
        if (!this.map) {
            this.pendingOperations.push(() => this.setZoom(zoom));
            return;
        }
        this.map.setZoom(Math.min(Math.max(zoom, this.minZoom), this.maxZoom));
    }

    resize() {
        if (!this.map) return;
        this.map.resize();
    }

    dispose() {
        if (this.map) {
            this.map.remove();
            this.map = null;
        }
        this.canvas = null;
        this.initialized = false;
        this.lastCenter = null;
    }

    getCanvas() {
        return this.canvas || (this.map ? this.map.getCanvas() : null);
    }

    async getMapTexture() {
        if (!this.map) return null;

        return new Promise(resolve => {
            if (this.canvas && this.map.loaded()) {
                resolve(this.canvas);
                return;
            }

            const check = () => {
                if (this.map.loaded()) {
                    this.canvas = this.map.getCanvas();
                    resolve(this.canvas);
                } else {
                    setTimeout(check, 100);
                }
            };
            check();
        });
    }
}

export const mapLibreManager = new MapLibreManager();