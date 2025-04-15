<script setup>
import {ref, computed} from 'vue'
import {marked} from 'marked'

const isModalOpen = ref(false)
const markdownContent = ref('')
const isLoading = ref(false)
const error = ref(null)

const browserLanguage = navigator.language.split('-')[0]
const savedLanguage = ref(localStorage.getItem('language') || browserLanguage || 'en')

const languageFilenames = {
  en: 'en_us',
  es: 'es_es'
}

const getChangelogUrl = () => {
  const lang = savedLanguage.value
  const filename = languageFilenames[lang] || languageFilenames.en
  return `https://raw.githubusercontent.com/muriplz/Caquita/main/changelog/${filename}.md`
}

const fetchChangelog = async () => {
  if (markdownContent.value) return

  isLoading.value = true
  error.value = null

  try {
    const response = await fetch(getChangelogUrl())
    if (!response.ok) throw new Error(`Failed to fetch: ${response.status}`)
    markdownContent.value = await response.text()
  } catch (err) {
    console.error('Error:', err)
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

const parsedMarkdown = computed(() => markdownContent.value ? marked(markdownContent.value) : '')
</script>

<template>
  <div>
    <button @click="openModal" class="cursor-pointer">
      <img src="/images/ui/changelog_button.png" alt="Changelog"/>
    </button>

    <Transition name="modal">
      <div v-if="isModalOpen" class="fixed inset-0 flex items-center justify-center" @click="closeModal">
        <div class="w-11/12 max-w-4xl max-h-[75vh] flex flex-col modal" @click.stop>
          <div class="flex justify-between items-center p-4 border-b">
            <h2 class="text-2xl">Changelog</h2>
            <button @click="closeModal" class="text-2xl cursor-pointer">&times;</button>
          </div>

          <div class="p-5 overflow-y-auto">
            <div v-if="isLoading" class="text-center">Loading changelog...</div>

            <div v-else-if="error" class="text-center">
              <p>Error: {{ error }}</p>
              <button @click="fetchChangelog" class="mt-2 px-4 py-2 bg-blue-500 text-white rounded">Retry</button>
            </div>

            <div v-else class="markdown-content" v-html="parsedMarkdown"></div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style>
.markdown-content {
  text-align: left;
}

.markdown-content h1 {
  font-size: 2rem;
  font-weight: bold;
  margin: 1.5rem 0 1rem 0;
}

.markdown-content h2 {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 1.25rem 0 0.75rem 0;
}

.markdown-content h3 {
  font-size: 1.25rem;
  font-weight: bold;
  margin: 1rem 0 0.5rem 0;
}

.markdown-content p {
  margin-bottom: 1rem;
  line-height: 1.6;
}

.markdown-content ul, .markdown-content ol {
  margin: 1rem 0;
  padding-left: 2rem;
}

.markdown-content ul {
  list-style-type: disc;
}

.markdown-content ol {
  list-style-type: decimal;
}

.markdown-content li {
  margin-bottom: 0.5rem;
}

.markdown-content a {
  color: #3182ce;
  text-decoration: underline;
}

.markdown-content blockquote {
  border-left: 4px solid #e2e8f0;
  padding-left: 1rem;
  margin: 1rem 0;
  color: #4a5568;
}

.markdown-content pre {
  background-color: #f7fafc;
  padding: 1rem;
  border-radius: 0.25rem;
  overflow-x: auto;
  margin: 1rem 0;
}

.markdown-content code {
  background-color: #f7fafc;
  padding: 0.2rem 0.4rem;
  border-radius: 0.25rem;
  font-family: monospace;
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from {
  opacity: 0;
  transform: translateY(-100%);
}

.modal-leave-to {
  opacity: 0;
  transform: translateY(100%);
}
</style>