<script setup>
import { ref, onMounted, computed } from 'vue'
import { marked } from 'marked'

const isModalOpen = ref(false)
const markdownContent = ref('')
const isLoading = ref(false)
const error = ref(null)

const browserLanguage = navigator.language.split('-')[0]
const savedLanguage = ref(localStorage.getItem('language') || browserLanguage || 'en')

const getChangelogUrl = () => {
  const lang = savedLanguage.value

  if (lang === 'es') {
    return 'https://raw.githubusercontent.com/muriplz/Caquita/main/changelog/es_es.md'
  } else {
    // Default to English for any other language
    return 'https://raw.githubusercontent.com/muriplz/Caquita/main/changelog/en_us.md'
  }
}

const fetchChangelog = async () => {
  if (markdownContent.value) return

  isLoading.value = true
  error.value = null

  try {
    const changelogUrl = getChangelogUrl()
    const response = await fetch(changelogUrl)

    if (!response.ok) {
      throw new Error(`Failed to fetch changelog: ${response.status} ${response.statusText}`)
    }

    markdownContent.value = await response.text()
  } catch (err) {
    console.error('Error fetching changelog:', err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

const openModal = () => {
  isModalOpen.value = true
  fetchChangelog()
}

const closeModal = () => {
  isModalOpen.value = false
}

const parsedMarkdown = computed(() => {
  return markdownContent.value ? marked(markdownContent.value) : ''
})
</script>

<template>
  <div>
    <button @click="openModal" class="">
      <img src="/images/ui/changelog_button.png" alt="Changelog" />
    </button>

    <div v-if="isModalOpen" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Changelog</h2>
          <button @click="closeModal" class="close-button">&times;</button>
        </div>

        <div class="modal-body">
          <div v-if="isLoading" class="loading">
            Loading changelog...
          </div>

          <div v-else-if="error" class="error">
            <p>Error loading changelog: {{ error }}</p>
            <button @click="fetchChangelog" class="retry-button">Retry</button>
          </div>

          <div v-else class="markdown-content" v-html="parsedMarkdown"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.changelog-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.changelog-button:active {
  transform: scale(0.95);
}


.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eaecef;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-button:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.error {
  text-align: center;
  padding: 20px;
  color: #e53935;
  border: 1px solid #ffcdd2;
  border-radius: 4px;
  background-color: #ffebee;
}

.retry-button {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.retry-button:hover {
  background-color: #1976d2;
}

.markdown-content {
  line-height: 1.6;
  text-align: left;
}

.markdown-content :deep(h1) {
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-content :deep(ul) {
  padding-left: 2em;
}

.markdown-content :deep(li) {
  margin: 0.25em 0;
}

.markdown-content :deep(code) {
  padding: 0.2em 0.4em;
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
  font-family: monospace;
}

.markdown-content :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}
</style>