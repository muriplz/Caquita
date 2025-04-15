// CreateAccountModal.vue
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import AuthService from '@/js/auth/AuthService.js';
import SyncService from '@/js/sync/SyncService.js';
import SyncStore from '@/js/sync/SyncStore.js';

const props = defineProps({
  username: String,
  password: String,
  show: Boolean
});

const loading = ref(false);
const router = useRouter();

const emit = defineEmits(['close']);

const handleRegister = async () => {
  console.log(props.username, props.password)
  if (!props.username || !props.password) return;

  loading.value = true;
  try {
    const success = await AuthService.register(props.username, props.password);
    if (success) {
      emit('close');
      await SyncService.init();
      await SyncStore.init();
      router.push('/game');
    } else {
      alert("Registration failed");
    }
  } catch (error) {
    console.error("Registration error:", error);
    alert("Registration failed. Please try again.");
  } finally {
    loading.value = false;
  }
};

const closeModal = () => {
  emit('close');
};
</script>

<template>
  <div v-if="show" class="fixed inset-0 flex items-center justify-center">
    <div class="p-4 modal m-4">
      <p>No users by that name. Wanna create an account?</p>
      <div class="flex justify-center mt-4 space-x-4">
        <img @click="closeModal" src="/images/ui/cancel_button.png" />
        <img @click="handleRegister" src="/images/ui/confirm_button.png" />
      </div>
    </div>
  </div>
</template>