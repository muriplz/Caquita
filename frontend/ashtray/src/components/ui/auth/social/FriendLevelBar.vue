<script setup>
import { onMounted, ref } from "vue";

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
  <div v-if="level" class="ml-3">
    <span class="text-xs font-bold mr-1.5">{{ level.level }}</span>
    <meter
        min="0"
        :max="level.total"
        :low="Math.floor(level.total * 0.2)"
        :high="Math.floor(level.total * 0.4)"
        :optimum="Math.floor(level.total * 0.9)"
        :value="level.progress"
        class="w-12">
    </meter>
  </div>
</template>