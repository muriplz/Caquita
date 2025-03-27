<template>
  <form class="auth-form" @submit.prevent="register">
    <div class="auth-header">
      <h2>Create an account</h2>
      <p>Please don't use personal information for the username, and remember the password you use. It's recommended to use a password you've never used before.</p>
    </div>

    <div class="input-group">
      <input v-model="registerUsername" type="text" placeholder="Username" />
    </div>

    <div class="input-group">
      <input v-model="registerPassword" type="password" placeholder="Password" />
    </div>

    <button type="submit" class="register-button">Sign Up</button>

    <div class="auth-footer">
      <a href="javascript:void(0)" @click="$emit('showAbout')">About us</a>
      <span class="divider">|</span>
      <a href="javascript:void(0)" @click="$emit('showLogin')">Already have an account</a>
    </div>
  </form>
</template>

<script setup>
import {ref} from 'vue'
import AuthService from "@/js/auth/AuthService.js"

const emit = defineEmits(['success', 'cancel', 'showLogin', 'showAbout'])

const registerUsername = ref('')
const registerPassword = ref('')

async function register() {
  const success = await AuthService.register(registerUsername.value, registerPassword.value)
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

.auth-header p {
  color: #666;
  font-size: 14px;
  margin-top: 10px;
  margin-bottom: -10px;
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

.register-button {
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

.register-button:hover {
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