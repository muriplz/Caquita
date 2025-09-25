<template>
  <div class="relative w-full h-96">
    <div class="absolute top-2 left-13 bg-white bg-opacity-90 px-3 py-2 rounded font-mono text-sm z-[1000] shadow-md">
      {{ latitude.toFixed(6) }}, {{ longitude.toFixed(6) }}
    </div>
    <div ref="mapContainer" class="w-full h-full rounded-lg overflow-hidden"></div>
    <Selector
        v-if="showSelector"
        :x="selectorPosition.x"
        :y="selectorPosition.y"
        :is-dragging="isDragging"
        @drag-start="handleSelectorDragStart"
        @drag-move="handleSelectorDragMove"
        @drag-end="handleSelectorDragEnd"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Selector from './Selector.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ lat: 40.4168, lng: -3.7038 })
  }
})

const emit = defineEmits(['update:modelValue'])

const mapContainer = ref(null)
const showSelector = ref(false)
const isDragging = ref(false)
const selectorPosition = ref({ x: 0, y: 0 })

let map = null

const latitude = computed(() => props.modelValue?.lat || 40.4168)
const longitude = computed(() => props.modelValue?.lng || -3.7038)

const updateModelValue = (lat, lng) => {
  emit('update:modelValue', { lat, lng })
}

const getUserLocation = () => {
  return new Promise((resolve) => {
    if (!navigator.geolocation) {
      resolve({ lat: 40.4168, lng: -3.7038 })
      return
    }

    navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          })
        },
        () => resolve({ lat: 40.4168, lng: -3.7038 }),
        { timeout: 5000 }
    )
  })
}

const initMap = async () => {
  const userLocation = await getUserLocation()
  const initialLat = props.modelValue?.lat || userLocation.lat
  const initialLng = props.modelValue?.lng || userLocation.lng

  map = L.map(mapContainer.value, {
    center: [initialLat, initialLng],
    zoom: 14,
    zoomControl: true,
    touchZoom: true,
    doubleClickZoom: true,
    scrollWheelZoom: true,
    boxZoom: true,
    keyboard: true,
    dragging: true
  })

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap'
  }).addTo(map)

  map.on('click', handleMapClick)
  map.on('move', updateSelectorPosition)
  map.on('zoom', updateSelectorPosition)

  if (props.modelValue?.lat && props.modelValue?.lng) {
    showSelector.value = true
    updateSelectorPosition()
  }
}

const handleMapClick = (e) => {
  if (isDragging.value) return

  const { lat, lng } = e.latlng
  updateModelValue(lat, lng)
  showSelector.value = true
  updateSelectorPosition()
}

const updateSelectorPosition = () => {
  if (!map || !showSelector.value || isDragging.value) return

  const point = map.latLngToContainerPoint([latitude.value, longitude.value])
  selectorPosition.value = { x: point.x, y: point.y }
}

const handleSelectorDragStart = () => {
  isDragging.value = true
  if (map) {
    map.dragging.disable()
    map.touchZoom.disable()
    map.doubleClickZoom.disable()
    map.scrollWheelZoom.disable()
    map.boxZoom.disable()
    map.keyboard.disable()
  }
}

const handleSelectorDragMove = ({ x, y }) => {
  if (!map || !isDragging.value) return

  const latlng = map.containerPointToLatLng([x, y])
  updateModelValue(latlng.lat, latlng.lng)
  selectorPosition.value = { x, y }
}

const handleSelectorDragEnd = () => {
  isDragging.value = false
  if (map) {
    map.dragging.enable()
    map.touchZoom.enable()
    map.doubleClickZoom.enable()
    map.scrollWheelZoom.enable()
    map.boxZoom.enable()
    map.keyboard.enable()
  }
}

watch(() => props.modelValue, (newValue) => {
  if (newValue && map && !isDragging.value) {
    map.setView([newValue.lat, newValue.lng])
    showSelector.value = true
    updateSelectorPosition()
  }
}, { deep: true })

onMounted(() => {
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
  document.head.appendChild(link)

  const script = document.createElement('script')
  script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
  script.onload = initMap
  document.head.appendChild(script)
})
</script>