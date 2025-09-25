<template>
  <div class="bg-white border-3 border-gray-800 p-6">
    <h2 class="text-xl font-semibold text-gray-600 mb-6">Messages</h2>

    <!-- Message Input -->
    <div class="flex gap-3 mb-6">
      <textarea
          v-model="message"
          placeholder="Type a message"
          rows="3"
          class="flex-1 p-3 border-2 border-gray-800 resize-none text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      ></textarea>
      <button
          @click="sendMessage"
          class="px-6 py-2 bg-gray-400 border-2 border-gray-800 hover:bg-gray-500 transition-colors text-sm self-start mt-2"
      >
        Send
      </button>
    </div>

    <!-- Messages List -->
    <div class="space-y-4">
      <div v-for="msg in messages" :key="msg.id" class="border-l-4 border-gray-300 pl-4 pb-4">
        <!-- Message Header -->
        <div class="flex justify-between items-start mb-2">
          <p class="text-sm flex-1 pr-4">
            <strong class="text-blue-600">@{{ msg.username }}:</strong>
            <span class="ml-2">{{ msg.content }}</span>
          </p>
          <button
              @click="toggleReplyForm(msg.id)"
              class="text-xs text-gray-500 hover:text-gray-700 transition-colors px-2 py-1"
          >
            reply
          </button>
        </div>

        <!-- Reply Form -->
        <div v-if="activeReplyId === msg.id" class="ml-6 mb-4 animate-pulse">
          <div class="flex gap-2">
            <textarea
                v-model="replyText"
                placeholder="Write your reply..."
                rows="1"
                class="flex-1 p-2 border-2 border-gray-600 resize-none text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
            ></textarea>
            <button
                @click="sendReply(msg.id)"
                class="px-3 py-1 bg-gray-400 border-2 border-gray-800 hover:bg-gray-500 transition-colors text-xs self-start"
            >
              Send
            </button>
          </div>
        </div>

        <!-- Replies -->
        <div v-if="msg.replies && msg.replies.length > 0" class="ml-6 space-y-2">
          <div
              v-for="reply in msg.replies"
              :key="reply.id"
              class="bg-gray-50 p-3 border-l-2 border-gray-300"
          >
            <p class="text-xs">
              <strong class="text-blue-600">@{{ reply.username }}:</strong>
              <span class="ml-2">{{ reply.content }}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'

const props = defineProps({
  messages: Array,
  petitionId: [String, Number]
})

const emit = defineEmits(['messages-updated'])

const message = ref('')
const replyText = ref('')
const activeReplyId = ref(null)

const sendMessage = async () => {
  await PetitionsApi.sendMessage(props.petitionId, message.value)
  message.value = ''
  emit('messages-updated')
}

const toggleReplyForm = (messageId) => {
  if (activeReplyId.value === messageId) {
    activeReplyId.value = null
  } else {
    activeReplyId.value = messageId
    replyText.value = ''
  }
}

const sendReply = async (messageId) => {
  await PetitionsApi.sendReply(messageId, replyText.value)
  replyText.value = ''
  activeReplyId.value = null
  emit('messages-updated')
}
</script>