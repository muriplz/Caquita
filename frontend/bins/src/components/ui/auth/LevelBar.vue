<script setup>
import {computed} from "vue";
import Store from "@/js/Store.js";

const level = computed(() => Store.getLevel());
const progressPercentage = computed(() => level.value ? level.value.getProgressPercentage() : 0);
</script>

<template>
  <div class="level-container" v-if="level">
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
.level-container {
  display: flex;
  align-items: center;
  width: 100%;
  margin-top: 10px;
}

.level-badge {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #4a6da7;
  color: white;
  font-size: 12px;
  font-weight: bold;
  position: relative;
  z-index: 1;
  margin-right: -12px;
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