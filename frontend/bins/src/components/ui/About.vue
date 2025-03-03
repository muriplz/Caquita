<template>
  <div class="about-form">
    <div class="about-header">
      <h2>About Us</h2>
    </div>

    <div class="about-content">
      <p>Welcome to Caquita. A game about inventory management with real world recycling landmarks.</p>

      <p>Find our monorepo on
      <a href="https://github.com/muriplz/Caquita">Github</a>
      </p>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about-form {
  background-color: white;
  padding: 30px;
  border-radius: 12px;
  max-width: 80%;
  min-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.about-header {
  text-align: center;
  margin-bottom: 10px;
}

.about-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.about-content {
  color: #333;
  line-height: 1.6;
}

.about-content h3 {
  margin: 20px 0 10px 0;
  font-size: 18px;
  color: #555;
}

.about-content p {
  margin: 0 0 15px 0;
}

.about-content ul {
  margin: 0 0 15px 20px;
  padding: 0;
}

.about-content li {
  margin-bottom: 5px;
}
</style>