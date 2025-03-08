// TileConversion.js - Fixed version that doesn't rely on reactive objects
import {reactive} from "vue";
import {defaultCoordinates, getCoordinates, resetCoordinates, setCoordinates} from './coordinates.js';

export const TILE_SIZE = 180;
export const DEFAULT_ZOOM = 15;

// Define the default world origin (Santander)
export const DEFAULT_ORIGIN = defaultCoordinates;

// Legacy reactive object - kept for backward compatibility
// but we won't use it internally anymore
export const WORLD_ORIGIN = reactive({
    lat: defaultCoordinates.lat,
    lon: defaultCoordinates.lon,
    isCustom: false
});

// Set the world origin using GPS coordinates
export function setWorldOrigin(lat, lon) {
    // Update non-reactive store
    setCoordinates(lat, lon);

    // ALSO update reactive object for backward compatibility
    WORLD_ORIGIN.lat = Number(lat);
    WORLD_ORIGIN.lon = Number(lon);
    WORLD_ORIGIN.isCustom = true;

    return getCoordinates();
}

// Reset world origin to default
export function resetWorldOrigin() {
    // Reset non-reactive store
    resetCoordinates();

    // ALSO update reactive object for backward compatibility
    WORLD_ORIGIN.lat = defaultCoordinates.lat;
    WORLD_ORIGIN.lon = defaultCoordinates.lon;
    WORLD_ORIGIN.isCustom = false;

    return getCoordinates();
}

// Convert latitude/longitude to tile coordinates (for map APIs)
export function latLonToTile(lat, lon, zoom = DEFAULT_ZOOM) {
    const n = Math.pow(2, zoom);
    const x = Math.floor(((lon + 180) / 360) * n);
    const y = Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * n);
    return { x, y, zoom };
}

// Convert tile coordinates to latitude/longitude
export function tileToLatLon(x, y, zoom = DEFAULT_ZOOM) {
    const n = Math.PI - 2 * Math.PI * y / Math.pow(2, zoom);
    const lat = 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    const lon = x / Math.pow(2, zoom) * 360 - 180;
    return { lat, lon };
}

// Convert lat/lon to game world coordinates
export function latLonToWorld(lat, lon, zoom = DEFAULT_ZOOM) {
    // Get current coordinates from our non-reactive store
    const origin = getCoordinates();

    // Calculate tile coordinates with high precision
    const { x: tileX, y: tileY } = latLonToTile(lat, lon, zoom);

    // Calculate the origin tile using our non-reactive store
    const { x: originTileX, y: originTileY } = latLonToTile(origin.lat, origin.lon, zoom);

    // Calculate precise tile offsets
    const tileOffsetX = tileX - originTileX;
    const tileOffsetY = tileY - originTileY;

    // Adjust for sub-tile precision
    const subTileX = ((lon + 180) / 360 * Math.pow(2, zoom)) % 1;
    const subTileY = (1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, zoom) % 1;

    // Convert tile offsets to world coordinates with sub-tile precision
    const worldX = (tileOffsetX + subTileX) * TILE_SIZE;
    const worldZ = (tileOffsetY + subTileY) * TILE_SIZE;

    return {x: worldX, z: worldZ};
}

// All other functions use getCoordinates() instead of WORLD_ORIGIN directly

// Convert game world coordinates to lat/lon
export function worldToLatLon(x, z, zoom = DEFAULT_ZOOM) {
    const origin = getCoordinates();

    // Calculate tile coordinates of the origin
    const { x: originTileX, y: originTileY } = latLonToTile(origin.lat, origin.lon, zoom);

    // Calculate tile offsets from world coordinates
    const tileOffsetX = x / TILE_SIZE;
    const tileOffsetY = z / TILE_SIZE;

    // Calculate absolute tile coordinates with sub-tile precision
    const tileX = originTileX + tileOffsetX;
    const tileY = originTileY + tileOffsetY;

    // Convert to lat/lon
    return tileToLatLon(tileX, tileY, zoom);
}

// Convert game world coordinates to tile coordinates
export function worldToTile(x, z, zoom = DEFAULT_ZOOM) {
    const { lat, lon } = worldToLatLon(x, z, zoom);
    return latLonToTile(lat, lon, zoom);
}

// Convert tile coordinates to game world coordinates
export function tileToWorld(tileX, tileY, zoom = DEFAULT_ZOOM) {
    const origin = getCoordinates();

    // Calculate the origin tile
    const { x: originTileX, y: originTileY } = latLonToTile(origin.lat, origin.lon, zoom);

    // Calculate tile offsets
    const tileOffsetX = tileX - originTileX;
    const tileOffsetY = tileY - originTileY;

    // Convert tile offsets to world coordinates
    const worldX = tileOffsetX * TILE_SIZE;
    const worldZ = tileOffsetY * TILE_SIZE;

    return { x: worldX, z: worldZ };
}

// Get the width and height of a tile in world units
export function getTileWorldSize() {
    return { width: TILE_SIZE, height: TILE_SIZE };
}

// Export current coordinates function for debugging
export { getCoordinates };