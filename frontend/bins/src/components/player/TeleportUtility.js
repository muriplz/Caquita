import {position, teleport} from './playerControls.js';
import {latLonToWorld, worldToLatLon} from '@/js/map/TileConversion.js';

/**
 * Utility for teleporting the player with smooth horizontal animations
 * or instant teleport for long distances (>1km)
 */
export default {
    /**
     * Teleport to world coordinates
     * @param {number} x - X coordinate
     * @param {number} z - Z coordinate
     * @param {number} duration - Animation duration in ms (default: 500ms)
     * @returns {Promise} Resolves when teleport completes
     */
    toWorldPosition(x, z, duration = 500) {
        return teleport({ x, z, y: 0, duration });
    },

    /**
     * Teleport to GPS coordinates
     * @param {number} lat - Latitude
     * @param {number} lon - Longitude
     * @param {number} duration - Animation duration in ms (default: 500ms)
     * @returns {Promise} Resolves when teleport completes
     */
    toLatLon(lat, lon, duration = 500) {
        const worldPos = latLonToWorld(lat, lon);
        return teleport({ x: worldPos.x, z: worldPos.z, y: 0, duration });
    },

    /**
     * Get current position as lat/lon
     * @returns {Object} {lat, lon}
     */
    getCurrentLatLon() {
        return worldToLatLon(position.x, position.z);
    }
};