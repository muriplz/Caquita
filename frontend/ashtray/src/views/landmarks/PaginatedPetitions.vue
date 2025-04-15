<script setup>
import { ref, onMounted, watch } from 'vue';
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";
import PaginatedItem from "@/views/landmarks/PaginatedItem.vue";

const petitions = ref([]);
const currentPage = ref(0); // Changed to 0 for zero-based indexing
const totalPages = ref(1);
const orderBy = ref('DESC');
const loading = ref(false);

const props = defineProps({
  status: {
    type: String,
    default: 'PENDING'
  }
})

const fetchPetitions = async () => {
  loading.value = true;
  try {
    const response = await PetitionsApi.get(currentPage.value, orderBy.value, props.status);

    petitions.value = response.petitions;
    totalPages.value = response.maxPages;
  } catch (error) {
    console.error('Error fetching petitions:', error);
  } finally {
    loading.value = false;
  }
};

const changePage = (page) => {
  currentPage.value = page - 1; // Adjust for zero-based indexing
};

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++;
  }
};

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--;
  }
};

watch(orderBy, () => {
  currentPage.value = 0;
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
          <PaginatedItem :petition="petition" />
        </li>
      </ul>

      <div class="pagination">
        <button
            @click="prevPage"
            :disabled="currentPage === 0"
            class="page-btn"
        >
          Previous
        </button>

        <div class="page-numbers">
          <button
              v-for="page in totalPages"
              :key="page"
              @click="changePage(page)"
              :class="['page-number', { active: page === currentPage + 1 }]"
          >
            {{ page }}
          </button>
        </div>

        <button
            @click="nextPage"
            :disabled="currentPage === totalPages - 1"
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
  min-width: calc(100vw - 20px);
  margin: 160px 0 0 0;
  padding-bottom: 40px;
}

.controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1rem;
}

.sort-select {
  padding: 0.5rem;
  border: 3px solid gray;
  font: inherit;
  font-size: 0.7rem;
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
  margin-bottom: 1rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 3px solid gray;
  background: white;
  cursor: pointer;
  border-radius: 0;
  font: inherit;
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
  border: 3px solid gray;
  background: white;
  cursor: pointer;
  border-radius: 0;
  font: inherit;
}

.page-number.active {
  background: #2e629c;
  color: white;
}

.no-results {
  text-align: center;
  padding: 2rem;
}

@media (max-width: 600px) {
  .petitions-container {
    margin: 290px auto 0 auto;
  }
}
</style>