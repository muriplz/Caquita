<script setup>
import {motion} from 'motion-v'
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import AuthService from '@/js/auth/AuthService.js';

const username = ref('');
const password = ref('');
const showCreateAccountModal = ref(false);
const loading = ref(false);
const router = useRouter();

const handleLogin = async () => {
  if (!username.value || !password.value || loading.value) return;

  loading.value = true;
  try {
    const response = await AuthService.login(username.value, password.value);
    if (response === null) {
      showCreateAccountModal.value = true;
    }
  } catch (error) {
    console.error("Login error:", error);
    alert("Login failed. Please try again.");
  } finally {
    loading.value = false;
  }

};

const handleRegister = async () => {
  if (!username.value || !password.value) return;

  loading.value = true;
  try {
    const success = await AuthService.register(username.value, password.value);
    if (success) {
      showCreateAccountModal.value = false;
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

const closeCreateAccountModal = () => {
  showCreateAccountModal.value = false;
  loading.value = false;
};
</script>

<template>
  <div class="login-background"></div>
  <div class="login-container">
    <div class="info-button-container">
      <img
          class="info-button"
          src="/images/ui/info_button.png"
      />
      <div class="tooltip">Enter your desired username if you don't have an account!</div>
    </div>
    <div class="login-form">
      <input
          v-model="username"
          type="text"
          placeholder=" username"
          @keyup.enter="handleLogin"
      />
      <input
          v-model="password"
          type="password"
          placeholder=" password"
          @keyup.enter="handleLogin"
      />
    </div>
    <img
        @click="handleLogin"
        class="login-button"
        :class="{ 'disabled': loading }"
        src="/images/ui/confirm_button.png"
    />

    <!-- Create Account Modal -->
    <div v-if="showCreateAccountModal" class="modal-overlay">
      <div class="modal-content">
        <p>No users by that name. Wanna create an account?</p>
        <div class="modal-buttons">
          <img
              @click="handleRegister"
              class="modal-button"
              src="/images/ui/confirm_button.png"
          />
          <img
              @click="closeCreateAccountModal"
              class="modal-button"
              src="/images/ui/cancel_button.png"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-image: url('/images/ui/login_background.png');
  background-size: auto 100%;
  background-position: center;
  background-repeat: no-repeat;
  image-rendering: pixelated;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  overflow-x: hidden;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.login-form input {
  outline: none;
  border: none;
}

.login-form input:focus {
  outline: none;
  border: none;
}

.info-button-container {
  position: relative;
  display: inline-block;
}

.info-button {
  height: 24px;
  cursor: pointer;
  image-rendering: pixelated;
}

.info-button:hover {
  transform: translateY(-2px);
}

.info-button:hover + .tooltip {
  visibility: visible;
  opacity: 1;
}

.tooltip {
  visibility: hidden;
  position: absolute;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  text-align: center;
  padding: 5px 10px;
  border-radius: 5px;
  z-index: 1;
  max-width: 200px;
  width: max-content;
  left: 50%;
  transform: translateX(-50%);
  bottom: 125%;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 14px;
  pointer-events: none;
  word-wrap: break-word;
}

.tooltip::after {
  content: "";
  position: absolute;
  top: 100%;
  left: 50%;
  margin-left: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: rgba(0, 0, 0, 0.7) transparent transparent transparent;
}

.login-button {
  align-self: center;
  height: 24px;
  cursor: pointer;
  image-rendering: pixelated;
  user-select: none;
}

.login-button:active {
  transform: translateY(2px);
}

.login-button.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.modal-content {
  background-color: #000000;
  padding: 15px;
  border-radius: 5px;
  text-align: center;
  image-rendering: pixelated;
  max-width: 80%;
}

.modal-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
}

.modal-button {
  height: 36px;
  cursor: pointer;
  image-rendering: pixelated;
}

.modal-button:active {
  transform: translateY(2px);
}

@media (max-width: 480px) {
  .tooltip {
    max-width: 90vw;
    left: 50%;
    transform: translateX(-50%);
    white-space: normal;
  }

  .modal-content {
    width: 90%;
  }
}
</style>