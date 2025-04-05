<script setup>
defineProps({
  friend: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <div v-if="friend.level" class="level-container">
    <span class="level-text">Lvl {{ friend.level.level }}</span>
    <meter min="0" :max="friend.level.total" low="30" high="75" optimum="85" :value="friend.level.progress"></meter>
    <span class="xp-text">{{ friend.level.progress }}/{{ friend.level.total }} XP</span>
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