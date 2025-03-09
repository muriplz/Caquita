import maplibregl from 'maplibre-gl';
import { getCoordinates } from './coordinates.js';
import { DEFAULT_ZOOM } from './TileConversion.js';

class MapLibreManager {
    constructor() {
        this.map = null;
        this.container = null;
        this.styleUrl = '/map_styles/caquita.json';
        this.initialized = false;
        this.maxZoom = 20;
        this.minZoom = 0;
    }

    async initialize(container) {
        if (this.initialized) return;

        this.container = container;
        const coordinates = getCoordinates();

        try {
            const styleResponse = await fetch(this.styleUrl);
            const style = await styleResponse.json();

            if (style.sources && style.sources.openmaptiles) {
                style.sources.openmaptiles = {
                    type: 'vector',
                    url: 'https://tiles-eu.stadiamaps.com/data/openmaptiles.json',
                    maxzoom: 14
                };
            }

            this.map = new maplibregl.Map({
                container: this.container,
                style: style,
                center: [coordinates.lon, coordinates.lat],
                zoom: DEFAULT_ZOOM,
                attributionControl: false,
                preserveDrawingBuffer: true,
                maxZoom: this.maxZoom,
                minZoom: this.minZoom
            });

            await new Promise(resolve => {
                this.map.on('load', resolve);
            });

            this.initialized = true;
            return this.map;
        } catch (error) {
            console.error('Error initializing MapLibre:', error);
            throw error;
        }
    }

    setCenter(lat, lon) {
        if (!this.map) return;
        this.map.setCenter([lon, lat]);
    }

    setZoom(zoom) {
        if (!this.map) return;
        this.map.setZoom(zoom);
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
        this.initialized = false;
    }

    getCanvas() {
        return this.map ? this.map.getCanvas() : null;
    }

    async getMapTexture() {
        if (!this.map) return null;

        return new Promise(resolve => {
            const check = () => {
                if (this.map.loaded()) {
                    const canvas = this.map.getCanvas();
                    resolve(canvas);
                } else {
                    setTimeout(check, 100);
                }
            };
            check();
        });
    }
}

export const mapLibreManager = new MapLibreManager();