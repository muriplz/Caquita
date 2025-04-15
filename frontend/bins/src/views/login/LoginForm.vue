<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import AuthService from '@/js/auth/AuthService.js';
import SyncService from '@/js/sync/SyncService.js';
import SyncStore from '@/js/sync/SyncStore.js';
import InfoButton from '@/views/login/InfoButton.vue';
import ChangelogMarkdown from "@/components/ChangelogMarkdown.vue";

// Accept props for v-model bindings
const props = defineProps({
  username: String,
  password: String
});

const loading = ref(false);
const router = useRouter();

// Update emits to include v-model events
const emit = defineEmits(['showCreateAccountModal', 'update:username', 'update:password']);

const handleLogin = async () => {
  const userInput = document.querySelector('input[type="text"]').value;
  const passInput = document.querySelector('input[type="password"]').value;

  if (!userInput || !passInput || loading.value) return;

  // Update parent state
  emit('update:username', userInput);
  emit('update:password', passInput);

  loading.value = true;
  try {
    const success = await AuthService.login(userInput, passInput);
    if (success === null) {
      emit('showCreateAccountModal');
    }

    if (success) {
      await SyncService.init();
      await SyncStore.init();
      router.push('/game')
    }
  } catch (error) {
    console.error("Login error:", error);
    alert("Login failed. Please try again.");
  } finally {
    loading.value = false;
  }
};
const updateUsername = (e) => emit('update:username', e.target.value);
const updatePassword = (e) => emit('update:password', e.target.value);
</script>

<template>
  <div class="flex flex-col items-center">
    <div class="flex items-center mb-2">
      <InfoButton class="mr-2" />
      <div class="flex flex-col">
        <input
            :value="username"
            @input="updateUsername"
            type="text"
            placeholder=" username"
            class="mb-2 w-40 modal"
        />
        <input
            :value="password"
            @input="updatePassword"
            type="password"
            placeholder=" password"
            class="w-40 modal"
        />
      </div>
      <ChangelogMarkdown class="ml-2" />
    </div>

    <button @click="handleLogin">
      <img
          :class="{ 'opacity-50': loading }"
          src="/images/ui/confirm_button.png"
      />
    </button>
  </div>
</template>