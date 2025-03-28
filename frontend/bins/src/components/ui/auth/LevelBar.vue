<script setup>
import SyncStore from "@/js/sync/SyncStore.js";

const level = SyncStore.getLevel();
</script>

<template>
  <div v-if="level" class="level-container">
    <span class="level-text">Lvl {{ level.level._value }}</span>
    <meter min="0" :max="level.levelTotal._value" low="30" high="75" optimum="85" :value="level.levelProgress._value"></meter>
    <span class="xp-text">{{ level.levelProgress._value }}/{{ level.levelTotal._value }} XP</span>
  </div>
</template>

<style scoped>
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