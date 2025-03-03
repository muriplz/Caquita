import { watch } from 'vue';
import { gpsWorldPosition } from './GPSTracker.js';
import { position, positionData } from './playerControls.js';
import settingsStore from '@/components/ui/settings/settings.js';

// Watch for GPS position changes and update player position if GPS is enabled
watch([gpsWorldPosition, () => settingsStore.settings.general.gpsEnabled], ([newPos, isEnabled]) => {
    if (isEnabled && gpsWorldPosition.isActive) {
        positionData.x = newPos.x;
        positionData.z = newPos.z;

        position.set(newPos.x, positionData.y, newPos.z);
    }
}, { deep: true });

// Export module just for completeness
export { };