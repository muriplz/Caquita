<template>
  <TresGroup>
    <TresPlane
        :position="position"
        :rotation="[0, 0, 0]"
        :scale="scale"
    >
      <TresMeshBasicMaterial :map="texture" :transparent="true" />
    </TresPlane>
  </TresGroup>
</template>

<script setup>
import {ref, watch} from 'vue'
import {useTexture} from '@tresjs/core'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  position: {
    type: Array,
    default: () => [0, 0, 0]
  },
  controls: {
    type: Object,
    default: () => ({})
  }
})

const texture = ref(null)
const scale = ref([1, 1, 1])

const loadTexture = async () => {
  if (!props.src) return

  texture.value = await useTexture(props.src)

  if (texture.value) {
    const imgAspect = texture.value.image.width / texture.value.image.height
    scale.value = [imgAspect, 1, 1]
  }
}

watch(() => props.src, loadTexture, { immediate: true })
</script>