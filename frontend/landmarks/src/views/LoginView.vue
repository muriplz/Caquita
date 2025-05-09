<script setup>
import { ref } from 'vue';
import LoginForm from '@/views/login/LoginForm.vue';
import CreateAccountModal from '@/views/login/CreateAccountModal.vue';

const showCreateAccountModal = ref(false);
const username = ref('');
const password = ref('');

const handleShowModal = () => {
  showCreateAccountModal.value = true;
};

const handleCloseModal = () => {
  showCreateAccountModal.value = false;
};
</script>

<template>
  <img
      src="/images/login_background.png"
      class="fixed h-screen w-auto object-contain max-w-none left-0 right-0 mx-auto"
  >

  <div class="fixed inset-0 flex items-center justify-center">
    <LoginForm
        @show-create-account-modal="handleShowModal"
        v-model:username="username"
        v-model:password="password"
    />
  </div>

  <CreateAccountModal
      :show="showCreateAccountModal"
      :username="username"
      :password="password"
      @close="handleCloseModal"
  />
</template>