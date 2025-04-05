<script setup>
const props = defineProps({
  show: Boolean,
  position: Object
});

const emit = defineEmits(['close']);

function closeUi() {
  emit('close');
}
</script>

<template>
  <div v-if="show" class="can-ui-container" @click.self="closeUi">
    <div class="can-ui" :style="{
      left: `${position.x}px`,
      top: `${position.y}px`,
      transform: 'translate(-50%, -50%)'
    }">
      <div class="header">
        <button class="close-btn" @click="closeUi">×</button>
      </div>
      <div class="content">
        Hello world
      </div>
    </div>
  </div>
</template>

<style scoped>
.can-ui-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 999;
  background-color: rgba(0,0,0,0.3);
  animation: fade-in 0.3s ease;
}

.can-ui {
  position: absolute;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  min-width: 240px;
  min-height: 140px;
  animation: scale-in 0.3s ease;
}

.header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.content {
  text-align: center;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 5px;
  line-height: 1;
  width: 40px;
  height: 40px;
}

.close-btn:hover {
  color: #000;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scale-in {
  from { transform: translate(-50%, -50%) scale(0.8); }
  to { transform: translate(-50%, -50%) scale(1); }
}
</style>