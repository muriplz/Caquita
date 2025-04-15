<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";
import PetitionsHeader from "@/views/landmarks/PetitionsHeader.vue";
import Store from "@/js/Store.js";
import AuthService from "@/js/auth/AuthService.js";

const route = useRoute();
const petitionId = ref(route.params.id);
const petition = ref(null);
const messages = ref([]);
const author = ref(null);
const message = ref('');
const replyText = ref('');
const activeReplyId = ref(null);

const fetchPetition = async () => {
  petition.value = await PetitionsApi.byId(petitionId.value);
};

const updateStatus = async (status) => {
  await PetitionsApi.updateStatus(petitionId.value, status);
  await fetchPetition();
};

const sendMessage = async () => {
  await PetitionsApi.sendMessage(petition.value.id, message.value);
  message.value = '';
  await fetchMessages();
};

const fetchMessages = async () => {
  const messageList = await PetitionsApi.messages(petitionId.value);

  // Fetch replies for each message
  for (const msg of messageList) {
    msg.replies = await PetitionsApi.replies(msg.id);
    msg.showReplyForm = false;
  }

  messages.value = messageList;
};

const toggleReplyForm = (messageId) => {
  if (activeReplyId.value === messageId) {
    activeReplyId.value = null;
  } else {
    activeReplyId.value = messageId;
    replyText.value = '';
  }
};

const sendReply = async (messageId) => {
  await PetitionsApi.sendReply(messageId, replyText.value);
  replyText.value = '';
  activeReplyId.value = null;
  await fetchMessages();
};

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-GB')
}

function getStatusColor(status) {
  switch(status) {
    case 'PENDING': return '#f0ad4e';
    case 'ACCEPTED': return '#5cb85c';
    case 'REJECTED': return '#d9534f';
    default: return 'gray';
  }
}

const fetchFeatures = () => {
  const landmarkInfo = { ...petition.value.landmarkInfo };
  delete landmarkInfo.name;
  return landmarkInfo;
};

onMounted(async () => {
  await fetchPetition();
  author.value = await AuthService.getUsername(petition.value.userId);
  await fetchMessages();
});
</script>

<template>
  <PetitionsHeader/>
  <div v-if="petition" class="petition-container">
    <div class="petition-info">
      <div class="petition-header">
        <img :src="PetitionsApi.getImageUrl(petition.id)" alt=""/>
        <div class="petition-header-title">
          <h2>{{ petition.landmarkInfo.name }}</h2>
          <h3 style="color:gray; font-size: 0.9rem;">{{ petition.type }}</h3>
          <h4 style="color:gray; font-size: 0.6rem">{{ formatDate(petition.creation) }}</h4>
          <h4 :style="{color: getStatusColor(petition.status), fontSize: '0.8rem'}">{{ petition.status }}</h4>
          <h4 style="color:gray; font-size: 0.8rem; margin-top: auto; margin-bottom: 24px;">By {{ author?.username }}</h4>
        </div>
      </div>
      <h3>Description</h3>
      <p>{{ petition.description }}</p>

      <hr style="color: gray; width: 80%; border-width: 1px; box-shadow: none; outline: none; border-style: solid;" />
      <div v-for="(feature, index) in fetchFeatures()" :key="index">{{ index.charAt(0).toUpperCase() + index.slice(1) }}: {{ feature }}</div>

      <p style="font-size: 0.7rem; color: gray;">At ({{ petition.lat }}, {{ petition.lon }})</p>

    </div>

    <div v-if="Store.getUser().trust === 'ADMINISTRATOR'" class="buttons">
      HI
      <button class="reject-button" @click="updateStatus('REJECTED')">Reject</button>
      <button class="accept-button" @click="updateStatus('ACCEPTED')">Accept</button>
      <button class="delete-button" @click="PetitionsApi.delete(petition.id)">Delete</button>
    </div>

    <h2 style="color: #a8a8a8">Messages</h2>
    <div class="message-input">
      <textarea v-model="message" placeholder="Type a message" rows="3"></textarea>
      <button @click="sendMessage">Send</button>
    </div>

    <div class="message-items">
      <div v-for="msg in messages" :key="msg.id" class="message-item">
        <div class="message-header">
          <p><strong>@{{ msg.username }}: </strong>{{ msg.content }}</p>
          <button class="reply-btn" @click="toggleReplyForm(msg.id)">reply</button>
        </div>

        <div v-if="activeReplyId === msg.id" class="reply-form slide-down">
          <textarea v-model="replyText" placeholder="Write your reply..." rows="1"></textarea>
          <button @click="sendReply(msg.id)">Send</button>
        </div>

        <div v-if="msg.replies && msg.replies.length > 0" class="replies">
          <div v-for="reply in msg.replies" :key="reply.id" class="reply-item">
            <p><strong>@{{ reply.username }}: </strong>{{ reply.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="loading">
    Loading petition...
  </div>
</template>

<style scoped>
.petition-container {
  padding: 20px;
  margin: 160px 0 0 0;
  width: 100vw;
}

.loading {
  text-align: center;
  padding: 40px;
}

.petition-info {
  background: #fff;
  width: calc(100vw - 40px);
  margin-right: 18px;
  border: 3px solid #535353;
}

.petition-header {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  gap: 15px;
}

.petition-header img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border: 3px solid #333;
  margin: 0;
  flex-shrink: 0;
}

.petition-header-title {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  flex-grow: 1;
  min-width: 0;
}

.petition-header-title h2 {
  margin-bottom: 5px;
}

.petition-header-title h3, h4 {
  margin-top: 0;
  white-space: nowrap;
}

.message-input {
  position: relative;
  display: flex;
  width: calc(100vw - 28px);
  margin-right: 18px;
  font-size: 0.7rem;
}

.message-input textarea {
  width: 100%;
  font: inherit;
}

.message-input button {
  background: #9f9f9f;
  border: 3px solid #333;
  font: inherit;
  margin: 10px;
  border-radius: 0;
}

.message-items {
  display: flex;
  flex-direction: column;
}

.message-item {
  background: #fff;
  width: calc(100vw - 40px);
  margin-right: 18px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  margin-top: 10px;
  align-items: flex-start;
  flex-grow: 1;
  min-height: 30px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

.message-item p {
  font-size: 0.8rem;
  margin: 0;
  width: calc(100% - 40px);
  word-wrap: break-word;
  text-align: justify;
}

.reply-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font: inherit;
  font-size: 0.5rem;
}

.reply-form {
  width: 95%;
  margin-left: auto;
  margin-top: 8px;
  display: flex;
}

.reply-form textarea {
  width: 100%;
  font: inherit;
  font-size: 0.6rem;
}

.reply-form button {
  align-self: center;
  font: inherit;
  font-size: 0.7rem;
  padding: 3px 8px;
  background: #9f9f9f;
  border: 2px solid #333;
  border-radius: 0;
  margin: 12px;
}

.replies {
  width: 95%;
  margin-left: auto;
  margin-top: 8px;
}

.reply-item {
  background: #f5f5f5;
  padding: 8px;
  margin-top: 5px;
  border-left: 2px solid #ccc;
}

.slide-down {
  animation: slideDown 0.3s ease-out forwards;
  overflow: hidden;
}

@keyframes slideDown {
  from {
    max-height: 0;
    opacity: 0;
  }
  to {
    max-height: 200px;
    opacity: 1;
  }
}

@media (max-width: 600px) {
  .petition-container {
    margin: 290px auto 0 auto;
  }
}

@media (max-width: 480px) {
  .petition-header {
    flex-direction: column;
    align-items: center;
  }

  .petition-header img {
    width: 90%;
    height: auto;
    aspect-ratio: 16/9;
    margin-bottom: 12px;
  }

  .petition-header-title {
    width: 100%;
    align-items: center;
  }
}


.buttons button{
  color: black;
  font: inherit;
}
</style>