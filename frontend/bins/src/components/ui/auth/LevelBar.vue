<script setup>
import SyncStore from "@/js/sync/SyncStore.js";

const level = SyncStore.getLevel();
</script>

<template>
  <div v-if="level" class="level-container">
    <span class="level-text">Lvl {{ level.level._value }}</span>
    <meter min="0" :max="level.total._value" low="30" high="75" optimum="85" :value="level.progress._value"></meter>
    <span class="xp-text">{{ level.progress._value }}/{{ level.total._value }} XP</span>
  </div>
</template>

<style scoped>
.level-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
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