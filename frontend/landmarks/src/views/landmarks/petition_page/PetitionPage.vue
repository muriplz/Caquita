<template>
  <PetitionsHeader/>
  <div v-if="petition" class="pt-40 px-5 min-h-screen bg-gray-50 pb-6">
    <div class="max-w-4xl mx-auto">
      <!-- Petition Header - Centered -->
      <div class="bg-white border-3 border-gray-800 p-6 mb-6">
        <div class="flex flex-col md:flex-row items-center md:items-start gap-6">
          <img
              :src="PetitionsApi.getImageUrl(petition.id)"
              alt=""
              class="w-32 h-32 object-cover border-3 border-gray-800 flex-shrink-0"
          />
          <div class="flex flex-col items-center md:items-start text-center md:text-left flex-grow">
            <h2 class="text-2xl font-bold mb-2">{{ petition.name }}</h2>
            <h3 class="text-gray-600 text-sm mb-1">{{ petition.type }}</h3>
            <h4 class="text-gray-500 text-xs mb-2">{{ formatDate(petition.creation) }}</h4>
            <h4
                :style="{color: getStatusColor(petition.status)}"
                class="text-sm font-semibold mb-2"
            >
              {{ petition.status }}
            </h4>
            <h4 class="text-gray-600 text-sm mt-auto">By {{ author?.username }}</h4>
          </div>
        </div>
      </div>

      <!-- Description -->
      <div class="bg-white border-3 border-gray-800 p-6 mb-6">
        <h3 class="text-xl font-semibold mb-4">Description</h3>
        <p class="text-gray-700 leading-relaxed">{{ petition.description }}</p>
      </div>

      <FeaturesSection
          :features="petition.info"
          :latitude="petition.lat"
          :longitude="petition.lon"
      />

      <AdminActions
          v-if="store.getUser.trust === 'ADMINISTRATOR'"
          @update-status="updateStatus"
          @delete-petition="() => PetitionsApi.delete(petition.id)"
      />

      <ChatComponent
          :messages="messages"
          :petition-id="petition.id"
          @messages-updated="fetchMessages"
      />
    </div>
  </div>

  <div v-else class="pt-40 text-center">
    <div class="text-lg text-gray-600">Loading petition...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js"
import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue"
import AuthService from "@/js/auth/AuthService.js"
import { useUserStore } from "@/js/Store.js"
import ChatComponent from "./ChatComponent.vue"
import AdminActions from "./AdminActions.vue"
import FeaturesSection from "./FeaturesSection.vue"

const store = useUserStore()
const route = useRoute()

const petitionId = ref(route.params.id)
const petition = ref(null)
const messages = ref([])
const author = ref(null)

const fetchPetition = async () => {
  petition.value = await PetitionsApi.byId(petitionId.value)
}

const updateStatus = async (status) => {
  await PetitionsApi.updateStatus(petitionId.value, status)
  await fetchPetition()
}

const fetchMessages = async () => {
  const messageList = await PetitionsApi.messages(petitionId.value)

  for (const msg of messageList) {
    msg.replies = await PetitionsApi.replies(msg.id)
  }

  messages.value = messageList
}

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-GB')
}

function getStatusColor(status) {
  switch(status) {
    case 'PENDING': return '#f0ad4e'
    case 'ACCEPTED': return '#5cb85c'
    case 'REJECTED': return '#d9534f'
    default: return 'gray'
  }
}

onMounted(async () => {
  await fetchPetition()
  author.value = await AuthService.getUsername(petition.value.userId)
  await fetchMessages()
})
</script>