<script setup>
import {computed, onMounted, onUnmounted, reactive, ref, watch} from 'vue';
import {positionData} from './player/playerControls.js';
import {tileLoader} from '../js/map/TileLoader.js';
import {WORLD_ORIGIN} from "../js/map/TileConversion.js";
import settingsManager from '@/components/ui/settings/settings.js';
import * as THREE from 'three';

// Configuration
const config = {
  tileSize: 180,
  zoomLevel: 15,
  centerLat: WORLD_ORIGIN.lat,
  centerLon: WORLD_ORIGIN.lon,
};

// Calculate center tile
const centerTile = {
  x: Math.floor((config.centerLon + 180) / 360 * Math.pow(2, config.zoomLevel)),
  y: Math.floor((1 - Math.log(Math.tan(config.centerLat * Math.PI / 180) + 1 / Math.cos(config.centerLat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, config.zoomLevel))
};

// State
const tiles = ref([]);
const debugMode = ref(false);
let isComponentMounted = false;

// Track materials for cleanup
const materials = reactive(new Map());
const textures = reactive(new Map());

// Get render distance from settings
const renderDistance = computed(() => {
  return settingsManager.settings.graphics.renderDistance;
});

// Compute current player tile position
const playerTilePosition = computed(() => {
  return {
    x: Math.floor(positionData.x / config.tileSize),
    z: Math.floor(positionData.z / config.tileSize)
  };
});

// Watch for player movement and update tiles
watch(playerTilePosition, () => {
  if (isComponentMounted) {
    updateTiles();
  }
}, {immediate: false});

// Watch for render distance changes
watch(renderDistance, () => {
  if (isComponentMounted) {
    updateTiles();
  }
}, {immediate: false});

onMounted(() => {
  isComponentMounted = true;
  updateTiles();

  // Debug toggle
  const toggleDebug = (e) => {
    if (e.key === 'F4') {
      debugMode.value = !debugMode.value;
    }
  };

  window.addEventListener('keydown', toggleDebug);
});

onUnmounted(() => {
  isComponentMounted = false;
  window.removeEventListener('keydown', toggleDebug);

  // Clean up all materials and textures
  cleanupUnusedResources([]);
});

// Create material and load texture for a tile
function createMaterial(tile) {
  const tileId = tile.id;

  // Create a basic material
  const material = new THREE.MeshBasicMaterial({transparent: true});
  materials.set(tileId, material);

  // Load the texture directly
  tileLoader.loadTile(tile.zoom, tile.mapX, tile.mapY, {
    priority: 'high'
  })
      .then(texture => {
        if (materials.has(tileId)) {
          material.map = texture;
          material.needsUpdate = true;
          textures.set(tileId, texture);
        }
      });

  return material;
}

// Update tiles based on player position
function updateTiles() {
  if (!isComponentMounted) return;

  const playerX = playerTilePosition.value.x;
  const playerZ = playerTilePosition.value.z;
  const viewDist = renderDistance.value;
  const newTiles = [];

  // Generate tiles around player
  for (let x = playerX - viewDist; x <= playerX + viewDist; x++) {
    for (let z = playerZ - viewDist; z <= playerZ + viewDist; z++) {
      // Convert to map tile coordinates
      const mapTileX = centerTile.x + x;
      const mapTileY = centerTile.y + z;

      // World position
      const worldX = x * config.tileSize + config.tileSize / 2;
      const worldZ = z * config.tileSize + config.tileSize / 2;

      const tileId = `${mapTileX}_${mapTileY}`;

      newTiles.push({
        id: tileId,
        position: [worldX, 0, worldZ],
        mapX: mapTileX,
        mapY: mapTileY,
        zoom: config.zoomLevel
      });
    }
  }

  // Update state
  tiles.value = newTiles;

  // Cleanup unused resources
  cleanupUnusedResources(newTiles.map(tile => tile.id));
}

// Clean up unused materials and textures
function cleanupUnusedResources(activeTileIds) {
  const activeIds = new Set(activeTileIds);

  // Clean up materials and textures not in active tiles
  for (const id of materials.keys()) {
    if (!activeIds.has(id)) {
      const material = materials.get(id);
      if (material) {
        material.dispose();
      }
      materials.delete(id);

      const texture = textures.get(id);
      if (texture) {
        texture.dispose();
      }
      textures.delete(id);
    }
  }
}
</script>

<template>
  <!-- Render ground tiles -->
  <TresMesh
      v-for="tile in tiles"
      :key="tile.id"
      :position="tile.position"
      :rotation="[-Math.PI / 2, 0, 0]"
  >
    <TresPlaneGeometry :args="[config.tileSize, config.tileSize]"/>
    <!-- Use direct materials instead of MaterialManager -->
    <primitive :object="materials.get(tile.id) || createMaterial(tile)"/>

    <!-- Debug tile overlay -->
    <TresMesh
        v-if="debugMode"
        :position="[0, 0.01, 0]"
    >
      <TresPlaneGeometry :args="[config.tileSize - 1, config.tileSize - 1]"/>
      <TresMeshBasicMaterial wireframe color="red" transparent :opacity="0.5"/>
    </TresMesh>
  </TresMesh>

  <!-- Debug overlay -->
  <div v-if="debugMode" class="debug-overlay">
    <div>Player Position: {{ positionData.x.toFixed(0) }}, {{ positionData.z.toFixed(0) }}</div>
    <div>Player Tile: {{ playerTilePosition.x }}, {{ playerTilePosition.z }}</div>
    <div>Zoom: {{ config.zoomLevel }}</div>
    <div>Render Distance: {{ renderDistance }}</div>
    <div>Tiles: {{ tiles.length }}</div>
  </div>
</template>

<style scoped>
.debug-overlay {
  position: absolute;
  top: 50px;
  right: 10px;
  padding: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  border-radius: 4px;
  font-size: 12px;
  z-index: 1000;
}
</style>