<script setup>
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";

const props = defineProps({
  petition: Object
})

const goToLandmark = () => {
  window.location.href = '/landmarks/' + props.petition.id;
}
</script>

<template>
  <div class="container" @click="goToLandmark()">
    <img :src="PetitionsApi.getImageUrl(petition.id)" alt="Landmark image" />
    <div class="title">
      <h3>{{ petition.name }}</h3>
      <h3 style="color: gray; font-size: 0.8rem;">{{ petition.type }}</h3>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 15px;
  background-color: #ccc;
  border: 3px solid gray;
  margin-bottom: 1rem;
  cursor: pointer;
  display: flex;
  max-width: calc(100vw - 20px);
}

img {
  width: 100px;
  height: 100px;
  min-width: 100px;
  min-height: 100px;
  object-fit: cover;
  image-rendering: pixelated;
  border: 3px solid #333;
}

.title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
  margin-left: 12px;
}

</style>