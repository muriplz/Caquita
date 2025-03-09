import { getCoordinates, resetCoordinates, setCoordinates } from './coordinates.js';

export const TILE_SIZE = 256;
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
    const { x: tileX, y: tileY } = latLonToTile(lat, lon, zoom);
    const { x: originTileX, y: originTileY } = latLonToTile(origin.lat, origin.lon, zoom);
    const tileOffsetX = tileX - originTileX;
    const tileOffsetY = tileY - originTileY;
    const subTileX = ((lon + 180) / 360 * Math.pow(2, zoom)) % 1;
    const subTileY = (1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, zoom) % 1;
    const worldX = (tileOffsetX + subTileX) * TILE_SIZE;
    const worldZ = (tileOffsetY + subTileY) * TILE_SIZE;
    return { x: worldX, z: worldZ };
}

export function worldToLatLon(x, z, zoom = DEFAULT_ZOOM) {
    const origin = getCoordinates();
    const { x: originTileX, y: originTileY } = latLonToTile(origin.lat, origin.lon, zoom);
    const tileOffsetX = x / TILE_SIZE;
    const tileOffsetY = z / TILE_SIZE;
    const tileX = originTileX + tileOffsetX;
    const tileY = originTileY + tileOffsetY;
    return tileToLatLon(tileX, tileY, zoom);
}

export function worldToTile(x, z, zoom = DEFAULT_ZOOM) {
    const { lat, lon } = worldToLatLon(x, z, zoom);
    return latLonToTile(lat, lon, zoom);
}

export function tileToWorld(tileX, tileY, zoom = DEFAULT_ZOOM) {
    const origin = getCoordinates();
    const { x: originTileX, y: originTileY } = latLonToTile(origin.lat, origin.lon, zoom);
    const tileOffsetX = tileX - originTileX;
    const tileOffsetY = tileY - originTileY;
    const worldX = tileOffsetX * TILE_SIZE;
    const worldZ = tileOffsetY * TILE_SIZE;
    return { x: worldX, z: worldZ };
}

export function getTileWorldSize() {
    return { width: TILE_SIZE, height: TILE_SIZE };
}

export { getCoordinates };