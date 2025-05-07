<script setup>
import {onMounted, ref} from "vue";
import LandmarksApi from "../../js/landmarks/LandmarksApi.js";
import AuthService from "../../js/auth/AuthService.js";

const props = defineProps({
  data: Object
});

const emit = defineEmits(['close']);

const landmark = ref(null);
const author = ref(null);

function closeUi() {
  emit('close');
}

onMounted(async () => {
  landmark.value = await LandmarksApi.getLandmark(props.data.id);
  author.value = await AuthService.getUsername(landmark.value.author)
});

</script>

<template>
  <div class="can-ui-container" @click.self="closeUi">
    <div class="can-ui">
      <div class="content">
        <img src="/images/ui/landmark/trash_can.png" alt=""/>
        <img src="/images/ui/landmark/trash_can_ashtray.png" alt=""/>
      </div>
    </div>
  </div>
</template>

<style>
.can-ui-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  background-color: rgba(0, 0, 0, 0.3);
  pointer-events: auto;
}

.can-ui {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  min-width: 240px;
  min-height: 140px;
}

.content {
  text-align: center;
  font-size: 18px;
}

.content img {
  position: absolute;
}
</style>