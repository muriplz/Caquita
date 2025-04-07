<script setup>

import {onMounted, ref} from "vue";

const level = ref();
const props = defineProps({
  friend: {
    type: Object,
    required: true
  }
})

onMounted(() => {
  level.value = JSON.parse(props.friend.level.value)
})
</script>

<template>
  <div v-if="level" class="level-container">
    <span class="level-text">{{ level.level }}</span>
    <meter
        min="0"
        :max="level.total"
        :low="Math.floor(level.total * 0.2)"
        :high="Math.floor(level.total * 0.4)"
        :optimum="Math.floor(level.total * 0.9)"
        :value="level.progress">
    </meter>
  </div>
</template>

<style scoped>
.level-container {
  margin-left: 12px;
}

meter {
  width: 50px;
}

.level-text {
  font-size: 12px;
  font-weight: bold;
  margin-right: 6px;
}
</style>