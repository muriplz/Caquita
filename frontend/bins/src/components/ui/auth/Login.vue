<template>
  <form class="auth-form" @submit.prevent="login">
    <div class="auth-header">
      <h2>Log In</h2>
    </div>

    <div class="input-group">
      <input v-model="loginUsername" type="text" placeholder="Username" />
    </div>

    <div class="input-group">
      <input v-model="loginPassword" type="password" placeholder="Password" />
    </div>

    <button type="submit" class="login-button">Sign In</button>

    <div class="auth-footer">
      <a href="javascript:void(0)" @click="$emit('showAbout')">About us</a>
      <span class="divider">|</span>
      <a href="javascript:void(0)" @click="$emit('showRegister')">Create an account</a>
    </div>
  </form>
</template>

<script setup>
import {ref} from 'vue'
import AuthService from "@/js/auth/authService.js"

const emit = defineEmits(['success', 'cancel', 'showRegister', 'showAbout'])

const loginUsername = ref('')
const loginPassword = ref('')

async function login() {
  const success = await AuthService.login(loginUsername.value, loginPassword.value)
  if (success) {
    emit('success')
  }
}
</script>

<style scoped>
.auth-form {
  background-color: white;
  padding: 30px;
  border-radius: 12px;
  width: 340px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.auth-header {
  text-align: center;
  margin-bottom: 10px;
}

.auth-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.auth-form input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.auth-form input:focus {
  outline: none;
  border-color: #555;
}

.login-button {
  padding: 12px;
  background-color: #333;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.login-button:hover {
  background-color: #444;
}

.auth-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.auth-footer a {
  color: #555;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.auth-footer a:hover {
  color: #000;
}

.divider {
  color: #ddd;
}
</style>