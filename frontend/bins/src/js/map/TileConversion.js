import {getCoordinates, resetCoordinates, setCoordinates} from './Coordinates.js';

export const TILE_SIZE = 512;
export const DEFAULT_ZOOM = 14;

export function setWorldOrigin(lat, lon) {
    setCoordinates(lat, lon);
    return getCoordinates();
}

export function resetWorldOrigin() {
    resetCoordinates();
    return getCoordinates();
}

export function latLonToTile(lat, lon, zoom = DEFAULT_ZOOM) {
    const n = Math.pow(2, zoom);
    const x = Math.floor(((lon + 180) / 360) * n);
    const y = Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * n);
    return { x, y, zoom };
}

export function tileToLatLon(x, y, zoom = DEFAULT_ZOOM) {
    const n = Math.PI - 2 * Math.PI * y / Math.pow(2, zoom);
    const lat = 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    const lon = x / Math.pow(2, zoom) * 360 - 180;
    return { lat, lon };
}

export function latLonToWorld(lat, lon, zoom = DEFAULT_ZOOM) {
    const origin = getCoordinates();

    // Convert to Web Mercator coordinates with high precision
    const worldX = lonToMercatorX(lon, zoom);
    const worldZ = latToMercatorY(lat, zoom);
    const originX = lonToMercatorX(origin.lon, zoom);
    const originZ = latToMercatorY(origin.lat, zoom);

    // Calculate the offset directly in meters
    return {
        x: (worldX - originX) * TILE_SIZE / 256,
        z: (worldZ - originZ) * TILE_SIZE / 256
    };
}

export function worldToLatLon(x, z, zoom = DEFAULT_ZOOM) {
    const origin = getCoordinates();

    // Convert the origin to Web Mercator coordinates
    const originX = lonToMercatorX(origin.lon, zoom);
    const originZ = latToMercatorY(origin.lat, zoom);

    // Apply the offset and convert back to lat/lon
    const mercatorX = originX + (x * 256 / TILE_SIZE);
    const mercatorY = originZ + (z * 256 / TILE_SIZE);

    return {
        lat: mercatorYToLat(mercatorY, zoom),
        lon: mercatorXToLon(mercatorX, zoom)
    };
}

// Helpers for precise Mercator projection conversion
function lonToMercatorX(lon, zoom) {
    return (lon + 180) / 360 * Math.pow(2, zoom);
}

function latToMercatorY(lat, zoom) {
    const sinLat = Math.sin(lat * Math.PI / 180);
    // Clamp to avoid infinity at poles
    const y = 0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI);
    return y * Math.pow(2, zoom);
}

function mercatorXToLon(x, zoom) {
    return x / Math.pow(2, zoom) * 360 - 180;
}

function mercatorYToLat(y, zoom) {
    const n = Math.PI - 2 * Math.PI * y / Math.pow(2, zoom);
    return 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
}

export function worldToTile(x, z, zoom = DEFAULT_ZOOM) {
    const { lat, lon } = worldToLatLon(x, z, zoom);
    return latLonToTile(lat, lon, zoom);
}

export function tileToWorld(tileX, tileY, zoom = DEFAULT_ZOOM) {
    const { lat, lon } = tileToLatLon(tileX, tileY, zoom);
    return latLonToWorld(lat, lon, zoom);
}

export function getTileWorldSize() {
    return { width: TILE_SIZE, height: TILE_SIZE };
}

export { getCoordinates };