<template>
  <div class="bg-white border-3 border-gray-800 p-6 mb-6">
    <h3 class="text-xl font-semibold mb-4">Admin Actions</h3>
    <div class="flex flex-wrap gap-3">
      <button
          @click="$emit('update-status', 'REJECTED')"
          class="px-4 py-2 bg-red-500 text-white border-2 border-gray-800 hover:bg-red-600 transition-colors"
      >
        Reject
      </button>
      <button
          @click="$emit('update-status', 'ACCEPTED')"
          class="px-4 py-2 bg-green-500 text-white border-2 border-gray-800 hover:bg-green-600 transition-colors"
      >
        Accept
      </button>
      <button
          @click="$emit('delete-petition')"
          class="px-4 py-2 bg-gray-700 text-white border-2 border-gray-800 hover:bg-gray-800 transition-colors"
      >
        Delete
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineEmits } from 'vue'

const emit = defineEmits(['update-status', 'delete-petition'])
</script>