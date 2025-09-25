<template>
  <div class="bg-white border-3 border-gray-800 p-6 mb-6">
    <h3 class="text-xl font-semibold mb-4">Features</h3>
    <div class="grid grid-cols-2 md:grid-cols-3 gap-2">
      <div v-for="(feature, index) in features" :key="index" class="text-sm">
        <span class="font-medium">{{ index.charAt(0).toUpperCase() + index.slice(1) }}:</span>
        <span :class="feature ? 'text-green-600' : 'text-red-600'" class="ml-1">
          {{ feature ? 'Yes' : 'No' }}
        </span>
      </div>
    </div>
    <div class="flex items-center gap-2 mt-4">
      <p class="text-xs text-gray-500">
        Location: ({{ latitude }}, {{ longitude }})
      </p>
      <a
          :href="`https://maps.google.com/?q=${latitude},${longitude}`"
          target="_blank"
          class="text-xs text-blue-600 hover:text-blue-800 underline"
      >
        View on Maps
      </a>
    </div>
  </div>
</template>

<script setup>
import {defineProps} from 'vue'

const props = defineProps({
  features: Object,
  latitude: [String, Number],
  longitude: [String, Number]
})
</script>