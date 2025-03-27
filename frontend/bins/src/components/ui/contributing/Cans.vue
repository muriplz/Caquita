<template>
  <div class="material-modal">
    <div class="material-header">
      <h2>Cans</h2>
    </div>
    <div class="material-content">
      <TopDownMapSection :initialLat="37.7749" :initialLng="-122.4194" />
    </div>
  </div>
</template>

<script setup>
import TopDownMapSection from "@/components/ui/contributing/TopDownMapSection.vue";
</script>

<style scoped>
.material-modal {
  background-color: white;
  padding: 30px;
  border-radius: 12px;
  max-width: 80%;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.material-header {
  text-align: center;
  margin-bottom: 10px;
}

.material-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.material-content {
  color: #333;
  line-height: 1.6;
}
</style>