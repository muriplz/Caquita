<template>
  <div v-if="level" class="level-container">
    <span class="level-text">{{ formatNumber(level.progress._value) }}</span>
    <meter
        min="0"
        :max="level.total._value"
        :low="Math.floor(level.total._value * 0.2)"
        :high="Math.floor(level.total._value * 0.4)"
        :optimum="Math.floor(level.total._value * 0.9)"
        :value="level.progress._value">
    </meter>
    <span class="level-text">{{ formatNumber(level.total._value) }}</span>
  </div>
</template>

<script setup>
import SyncStore from "@/js/sync/SyncStore.js";

const level = SyncStore.getLevel();

function formatNumber(num) {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}
</script>

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
}

meter {
  appearance: none;
  height: 10px;
  border-radius: 5px;
}

meter::-webkit-meter-bar {
  background: #e6e6e6;
  border-radius: 5px;
}

meter::-webkit-meter-optimum-value {
  background: #398c0e;
}

meter::-webkit-meter-suboptimum-value {
  background: #d59a4d;
}

meter::-webkit-meter-even-less-good-value {
  background: #df4e4e;
}

meter::-moz-meter-bar {
  background: #e6e6e6;
}

meter::-moz-meter-optimum {
  background: #398c0e;
}

meter::-moz-meter-sub-optimum {
  background: #d59a4d;
}

meter::-moz-meter-sub-sub-optimum {
  background: #df4e4e;
}
</style>