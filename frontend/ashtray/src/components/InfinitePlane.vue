// src/components/InfinitePlane.vue
<script setup>
import {onMounted, ref} from 'vue'
import * as THREE from 'three'
import {useTresContext} from '@tresjs/core'

const { scene, camera } = useTresContext()
const gridRef = ref(null)

onMounted(() => {
  const geometry = new THREE.PlaneGeometry(20000, 20000)
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uFadeDistance: { value: 900.0 },
      uColor: { value: new THREE.Color(0xBDBDBD) },
      uCameraPosition: { value: new THREE.Vector3() }
    },
    vertexShader: `
      varying vec3 vWorldPosition;
      void main() {
        vec4 worldPosition = modelMatrix * vec4(position, 1.0);
        vWorldPosition = worldPosition.xyz;
        gl_Position = projectionMatrix * viewMatrix * worldPosition;
      }
    `,
    fragmentShader: `
      uniform float uFadeDistance;
      uniform vec3 uColor;
      uniform vec3 uCameraPosition;
      varying vec3 vWorldPosition;

      void main() {
        vec3 toPoint = vWorldPosition - uCameraPosition;
        toPoint.y = 0.0; // Project to xz plane
        float dist = length(toPoint);
        float fade = 1.0 - smoothstep(uFadeDistance * 0.7, uFadeDistance, dist);
        gl_FragColor = vec4(uColor, fade);
      }
    `,
    transparent: true,
    side: THREE.DoubleSide,
    depthWrite: false,
    depthTest: true,
    polygonOffset: true,
    polygonOffsetFactor: -1,
    polygonOffsetUnits: -1
  })

  const mesh = new THREE.Mesh(geometry, material)
  mesh.rotation.x = -Math.PI / 2
  mesh.position.y = -5
  mesh.renderOrder = -1
  gridRef.value = mesh
  scene.value.add(mesh)

  // Update camera position in shader
  function updateCameraPosition() {
    if (camera.value) {
      material.uniforms.uCameraPosition.value.copy(camera.value.position)
    }
    requestAnimationFrame(updateCameraPosition)
  }
  updateCameraPosition()
})
</script>

<template>
</template>