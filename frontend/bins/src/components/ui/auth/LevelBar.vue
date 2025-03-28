<script setup>
import { computed, onMounted, ref } from "vue";
import SyncStore from "@/js/sync/SyncStore.js";
import SyncService from "@/js/sync/SyncService.js";

console.log("LevelBar component initializing");
const level = SyncStore.getLevel();
console.log("Level from SyncStore:", level.value);

const progressPercentage = computed(() => {
  console.log("Computing progress percentage, level is:", level.value);
  if (!level.value) return 0;
  try {
    const percentage = level.value.getProgressPercentage();
    console.log("Progress percentage:", percentage);
    return percentage;
  } catch (e) {
    console.error("Error calculating percentage:", e);
    return 0;
  }
});

// This is just for testing
const hasTriedDebug = ref(false);

onMounted(() => {
  console.log("LevelBar mounted");

  // Make sure sync client is initialized and subscribed to currencies
  try {
    const client = SyncService.getClient();
    console.log("Got sync client:", client);
    client.subscribe('currencies');
  } catch (e) {
    console.error("Error getting sync client:", e);
  }

  // Debug fallback to ensure we see something
  setTimeout(() => {
    if (!level.value && !hasTriedDebug.value) {
      console.log("No level data after 3s, setting debug data");
      hasTriedDebug.value = true;

      // Create mock level data
      const mockLevelData = {
        level: 1,
        experience: 50,
        "level-progress": 50,
        "level-total": 100,
        "next-level-total": 110
      };

      SyncStore.debugSetLevel(mockLevelData);
    }
  }, 3000);
});
</script>

<template>
  <div class="level-debug" v-if="!level">Waiting for level data...</div>
  <div class="level-container" v-else>
    <div class="level-bar-container">
      <div class="level-bar" :style="{ width: progressPercentage + '%' }"></div>
      <div class="level-info">
        <span class="level-text">Level {{ level.level }}</span>
        <span class="xp-text">{{ level.levelProgress }}/{{ level.levelTotal }} XP</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.level-debug {
  padding: 10px;
  background-color: #ffdddd;
  border: 1px solid #ff0000;
  color: #ff0000;
  margin: 10px 0;
  text-align: center;
}

.level-container {
  display: flex;
  align-items: center;
  width: 100%;
  margin-top: 10px;
}

.level-bar-container {
  flex: 1;
  height: 20px;
  background-color: #e0e6f0;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.level-bar {
  height: 100%;
  background-color: #4a6da7;
  border-radius: 10px;
  transition: width 0.5s ease;
}

.level-info {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px 0 20px;
}

.level-text {
  font-size: 12px;
  font-weight: bold;
  color: #000000;
}

.xp-text {
  font-size: 10px;
  color: #000000;
}
</style>