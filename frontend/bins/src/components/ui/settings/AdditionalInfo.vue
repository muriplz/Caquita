<template>
  <div class="info-container">
    <div class="info-icon" @click="showModal = true">?</div>

    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showModal" class="modal-overlay" @click="showModal = false">
          <div class="modal-content" @click.stop>
            <div class="modal-body">
              {{ message }}
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import {ref} from 'vue';

const props = defineProps({
  message: {
    type: String,
    required: true
  }
});

const showModal = ref(false);
</script>

<style scoped>
.info-container {
  display: inline-block;
  position: relative;
}

.info-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #777;
  color: white;
  font-size: 14px;
  cursor: pointer;
  margin-left: 8px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background-color: #222;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  padding: 20px;
  color: white;
  position: relative;
  z-index: 2001;
}

.modal-fade-enter-active {
  animation: modal-in 0.3s;
}

.modal-fade-leave-active {
  animation: modal-out 0.3s;
}

@keyframes modal-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes modal-out {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

.modal-content {
  animation: slide-in 0.5s;
}

.modal-fade-leave-active .modal-content {
  animation: slide-out 0.3s;
}

@keyframes slide-in {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slide-out {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(-100%);
    opacity: 0;
  }
}
</style>