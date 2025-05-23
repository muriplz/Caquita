<script setup>
import { ref, onMounted, watch } from 'vue';
import PetitionsApi from "@/views/landmarks/js/PetitionsApi.js";
import PaginatedItem from "@/views/landmarks/PaginatedItem.vue";

const petitions = ref([]);
const currentPage = ref(0);
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
  currentPage.value = page - 1;
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
  <div class="mt-50 px-4 min-h-[calc(100vh-12.5rem)] flex flex-col">
    <div class="mb-6">
      <select v-model="orderBy" class="px-3 py-2 border rounded">
        <option value="DESC">Newest first</option>
        <option value="ASC">Oldest first</option>
      </select>
    </div>

    <div class="flex-1">
      <div v-if="loading" class="text-center py-8">Loading...</div>

      <div v-else>
        <div v-if="petitions.length === 0" class="text-center py-8">No petitions found</div>

        <ul v-else class="space-y-4">
          <li v-for="petition in petitions" :key="petition.id">
            <PaginatedItem :petition="petition" />
          </li>
        </ul>
      </div>
    </div>

    <div class="flex justify-between items-center mt-8 py-4">
      <button
          @click="prevPage"
          :disabled="currentPage === 0"
          class="px-4 py-2 border disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Previous
      </button>

      <div class="flex gap-2">
        <button
            v-for="page in totalPages"
            :key="page"
            @click="changePage(page)"
            :class="['px-3 py-1 border', { 'bg-gray-300': page === currentPage + 1 }]"
        >
          {{ page }}
        </button>
      </div>

      <button
          @click="nextPage"
          :disabled="currentPage === totalPages - 1"
          class="px-4 py-2 border disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Next
      </button>
    </div>
  </div>
</template>