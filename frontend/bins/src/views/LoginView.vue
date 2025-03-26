<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { motion } from 'motion-v';
import AuthService from '@/js/auth/authService.js';

const username = ref('');
const password = ref('');
const showModal = ref(false);
const loading = ref(false);
const router = useRouter();

const handleLogin = async () => {
  if (!username.value || !password.value) return;

  loading.value = true;
  const success = await AuthService.login(username.value, password.value);
  loading.value = false;

  if (success) {
    router.push('/game');
  } else {
    showModal.value = true;
  }
};

const handleRegister = async () => {
  if (!username.value || !password.value) return;

  loading.value = true;
  const success = await AuthService.register(username.value, password.value);
  loading.value = false;

  if (success) {
    router.push('/game');
  }
};

const closeModal = () => {
  showModal.value = false;
};
</script>

<template>
  <div class="login-container">
    <motion.div
        :initial="{ opacity: 0, y: 20 }"
        :animate="{ opacity: 1, y: 0 }"
        :transition="{ duration: 0.4 }"
        class="login-card"
    >
      <motion.div
          :initial="{ opacity: 0, x: -20 }"
          :animate="{ opacity: 1, x: 0 }"
          :transition="{ delay: 0.1, duration: 0.3 }"
          class="input-group"
      >
        <input
            v-model="username"
            type="text"
            placeholder="Username"
            @keyup.enter="handleLogin"
        />
      </motion.div>

      <motion.div
          :initial="{ opacity: 0, x: -20 }"
          :animate="{ opacity: 1, x: 0 }"
          :transition="{ delay: 0.2, duration: 0.3 }"
          class="input-group"
      >
        <input
            v-model="password"
            type="password"
            placeholder="Password"
            @keyup.enter="handleLogin"
        />
      </motion.div>

      <motion.div
          :initial="{ opacity: 0 }"
          :animate="{ opacity: 1 }"
          :transition="{ delay: 0.3, duration: 0.3 }"
          class="button-container"
      >
        <button
            @click="handleLogin"
            :disabled="loading"
            class="login-button"
        >
          →
        </button>
      </motion.div>
    </motion.div>

    <!-- Registration Modal -->
    <div class="modal-backdrop" v-if="showModal" @click="closeModal">
      <motion.div
          :initial="{ scale: 0.8, opacity: 0 }"
          :animate="{ scale: 1, opacity: 1 }"
          :transition="{ duration: 0.3 }"
          class="modal"
          @click.stop
      >
        <h2>Account Not Found</h2>
        <p>Want to create a new account?</p>
        <div class="modal-buttons">
          <button @click="handleRegister" class="yes-button">Yes</button>
          <button @click="closeModal" class="no-button">No</button>
        </div>
      </motion.div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.login-card {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 320px;
}

.input-group {
  margin-bottom: 15px;
}

input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  transition: border-color 0.2s;
}

input:focus {
  border-color: #4a90e2;
  outline: none;
}

.button-container {
  display: flex;
  justify-content: flex-end;
}

.login-button {
  padding: 8px 16px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
  transition: background 0.2s;
}

.login-button:hover {
  background: #3a7bc8;
}

.login-button:disabled {
  background: #a0c4f0;
  cursor: not-allowed;
}

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.modal {
  background: white;
  border-radius: 10px;
  padding: 30px;
  width: 90%;
  max-width: 320px;
  text-align: center;
}

.modal h2 {
  margin-bottom: 10px;
  color: black;
}

.modal p {
  margin-bottom: 20px;
  color: black;
}

.modal-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.yes-button, .no-button {
  padding: 10px 25px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.yes-button {
  background: #4a90e2;
  color: white;
}

.no-button {
  background: #f1f1f1;
  color: #333;
}
</style>