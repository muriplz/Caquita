// TileConversion.js - With proper mathematical coordinate conversion
export const TILE_SIZE = 180;
export const DEFAULT_ZOOM = 15;

// Define the default world origin (Santander)
export const DEFAULT_ORIGIN = {
    lat: 43.4623,
    lon: -3.8098,
};

// Dynamic origin that can be set once when GPS is activated
export let WORLD_ORIGIN = {
    lat: DEFAULT_ORIGIN.lat,
    lon: DEFAULT_ORIGIN.lon,
    isCustom: false
};

// Set the world origin using GPS coordinates
export function setWorldOrigin(lat, lon) {
    WORLD_ORIGIN.lat = lat;
    WORLD_ORIGIN.lon = lon;
    WORLD_ORIGIN.isCustom = true;
    return WORLD_ORIGIN;
}

// Reset world origin to default (Badajoz)
export function resetWorldOrigin() {
    WORLD_ORIGIN.lat = DEFAULT_ORIGIN.lat;
    WORLD_ORIGIN.lon = DEFAULT_ORIGIN.lon;
    WORLD_ORIGIN.isCustom = false;
    return WORLD_ORIGIN;
}

// Convert latitude/longitude to tile coordinates (for map APIs)
export function latLonToTile(lat, lon, zoom = DEFAULT_ZOOM) {
    const x = Math.floor((lon + 180) / 360 * Math.pow(2, zoom));
    const y = Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, zoom));
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
    // Calculate tile coordinates
    const { x: tileX, y: tileY } = latLonToTile(lat, lon, zoom);

    // Calculate the origin tile
    const { x: originTileX, y: originTileY } = latLonToTile(WORLD_ORIGIN.lat, WORLD_ORIGIN.lon, zoom);

    // Calculate tile offsets
    const tileOffsetX = tileX - originTileX;
    const tileOffsetY = tileY - originTileY;

    // Convert tile offsets to world coordinates
    // Each tile is TILE_SIZE world units
    const worldX = tileOffsetX * TILE_SIZE;
    const worldZ = tileOffsetY * TILE_SIZE;

    return { x: worldX, z: worldZ };
}

// Convert game world coordinates to lat/lon
export function worldToLatLon(x, z, zoom = DEFAULT_ZOOM) {
    // Calculate tile coordinates of the origin
    const { x: originTileX, y: originTileY } = latLonToTile(WORLD_ORIGIN.lat, WORLD_ORIGIN.lon, zoom);

    // Calculate tile offsets from world coordinates
    const tileOffsetX = x / TILE_SIZE;
    const tileOffsetY = z / TILE_SIZE;

    // Calculate absolute tile coordinates
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
    // Calculate the origin tile
    const { x: originTileX, y: originTileY } = latLonToTile(WORLD_ORIGIN.lat, WORLD_ORIGIN.lon, zoom);

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