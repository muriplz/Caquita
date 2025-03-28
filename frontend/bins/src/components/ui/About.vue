<template>
  <div class="about-form">
    <div class="about-header">
      <h2>About Us</h2>
      <ChangelogMarkdown class="changelog"/>
      <p>Changelog</p>
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
import ChangelogMarkdown from "@/components/ChangelogMarkdown.vue";
</script>

<style scoped>
.about-form {
  background-color: white;
  padding: 30px;
  max-width: 80%;
  min-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  border: 3px solid black;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.about-header {
  text-align: center;
  display: flex;
  flex-direction: row;
  align-items: center;
  margin: 10px auto;
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

.changelog {
  margin-left: 40px;
  margin-right: 4px;
}
</style>