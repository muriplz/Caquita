<script setup>
import { ref, onMounted, watch } from 'vue';
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";

const petitions = ref([]);
const currentPage = ref(1);
const totalPages = ref(1);
const orderBy = ref('DESC');
const loading = ref(false);

const fetchPetitions = async () => {
  loading.value = true;
  try {
    const response = await PetitionsApi.get(currentPage.value, orderBy.value);
    petitions.value = response.data;
    totalPages.value = response.meta.totalPages;
  } catch (error) {
    console.error('Error fetching petitions:', error);
  } finally {
    loading.value = false;
  }
};

const changePage = (page) => {
  currentPage.value = page;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

watch(orderBy, () => {
  currentPage.value = 1;
  fetchPetitions();
});

watch(currentPage, () => {
  fetchPetitions();
});

onMounted(() => {
  fetchPetitions();
});
</script>

<template>
  <div class="petitions-container">
    <div class="controls">
      <select v-model="orderBy" class="sort-select">
        <option value="DESC">Newest first</option>
        <option value="ASC">Oldest first</option>
      </select>
    </div>

    <div v-if="loading" class="loader">Loading...</div>

    <div v-else>
      <div v-if="petitions.length === 0" class="no-results">No petitions found</div>

      <ul v-else class="petitions-list">
        <li v-for="petition in petitions" :key="petition.id" class="petition-item">
          <h3>{{ petition.title }}</h3>
          <p>{{ petition.description }}</p>
        </li>
      </ul>

      <div class="pagination">
        <button
            @click="prevPage"
            :disabled="currentPage === 1"
            class="page-btn"
        >
          Previous
        </button>

        <div class="page-numbers">
          <button
              v-for="page in totalPages"
              :key="page"
              @click="changePage(page)"
              :class="['page-number', { active: page === currentPage }]"
          >
            {{ page }}
          </button>
        </div>

        <button
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="page-btn"
        >
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.petitions-container {
  max-width: 800px;
  margin: 0 auto;
}

.controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1rem;
}

.sort-select {
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.loader {
  text-align: center;
  padding: 2rem;
}

.petitions-list {
  list-style: none;
  padding: 0;
}

.petition-item {
  border: 1px solid #eee;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  margin: 0 1rem;
}

.page-number {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 0.25rem;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
}

.page-number.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.no-results {
  text-align: center;
  padding: 2rem;
}
</style>