<script setup>
import { computed } from "vue";

const props = defineProps({
  row: {
    type: Number,
    required: true
  },
  col: {
    type: Number,
    required: true
  },
  index: {
    type: Number,
    required: true
  },
  isOccupied: {
    type: Boolean,
    default: false
  },
  connections: {
    type: Object,
    default: () => ({
      top: false,
      right: false,
      bottom: false,
      left: false
    })
  }
});

const connectionType = computed(() => {
  if (!props.isOccupied) return 'empty';

  const {top, right, bottom, left} = props.connections;
  const connectionCount = [top, right, bottom, left].filter(Boolean).length;

  if (connectionCount === 0) return 'isolated';

  if (connectionCount === 1) {
    if (top) return 'connected-top';
    if (right) return 'connected-right';
    if (bottom) return 'connected-bottom';
    if (left) return 'connected-left';
  }

  if (connectionCount === 2) {
    if (top && bottom) return 'connected-vertical';
    if (left && right) return 'connected-horizontal';
    if (top && right) return 'connected-top-right';
    if (top && left) return 'connected-top-left';
    if (bottom && right) return 'connected-bottom-right';
    if (bottom && left) return 'connected-bottom-left';
  }

  if (connectionCount === 3) {
    if (!top) return 'connected-three-bottom';
    if (!right) return 'connected-three-left';
    if (!bottom) return 'connected-three-top';
    if (!left) return 'connected-three-right';
  }

  return 'connected-all';
});

const getConnectionClasses = computed(() => {
  const classes = [connectionType.value];

  if (props.isOccupied) {
    classes.push('occupied');
  }

  return classes;
});
</script>

<template>
  <div
      class="grid-slot"
      :class="getConnectionClasses"
      :style="{
      gridRowStart: row + 1,
      gridColumnStart: col + 1,
      gridRowEnd: row + 2,
      gridColumnEnd: col + 2
    }"
      :data-index="index"
  ></div>
</template>

<style scoped>
.grid-slot {
  width: 64px;
  height: 64px;
  background-color: #525252;
  border: 2px solid #626262;
  border-radius: 24px;
  box-sizing: border-box;
  position: relative;
}

.grid-slot:hover:not(.occupied) {
  background-color: #626262;
  border-color: #727272;
}

.occupied {
  background-color: #383838;
  border-color: #727272;
}

.isolated {
  border-radius: 24px;
}

.connected-top {
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

.connected-right {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.connected-bottom {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

.connected-left {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.connected-vertical {
  border-radius: 0;
}

.connected-horizontal {
  border-radius: 0;
}

.connected-top-right {
  border-radius: 0;
  border-bottom-left-radius: 24px;
}

.connected-top-left {
  border-radius: 0;
  border-bottom-right-radius: 24px;
}

.connected-bottom-right {
  border-radius: 0;
  border-top-left-radius: 24px;
}

.connected-bottom-left {
  border-radius: 0;
  border-top-right-radius: 24px;
}

.connected-three-top {
  border-radius: 0;
  border-bottom-left-radius: 24px;
  border-bottom-right-radius: 24px;
}

.connected-three-right {
  border-radius: 0;
  border-top-left-radius: 24px;
  border-bottom-left-radius: 24px;
}

.connected-three-bottom {
  border-radius: 0;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
}

.connected-three-left {
  border-radius: 0;
  border-top-right-radius: 24px;
  border-bottom-right-radius: 24px;
}

.connected-all {
  border-radius: 0;
}
</style>