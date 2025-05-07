<template>
  <div class="info-row shape-row">
    <span>Shape:</span>
    <div class="shape-container" v-if="item?.shape">
      <div class="shape-grid">
        <div
            v-for="(row, rowIndex) in item.shape"
            :key="rowIndex"
            class="shape-row"
        >
          <div
              v-for="(cell, cellIndex) in row"
              :key="cellIndex"
              class="shape-cell"
              :class="{ 'filled': cell === 1 }"
          ></div>
        </div>
      </div>
    </div>
    <span v-else>Unknown</span>
  </div>
</template>

<script>
export default {
  props: {
    item: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
.info-row.shape-row {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  margin: 15px 0;
}

.shape-container {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
}

.shape-grid {
  display: inline-block;
  padding: 2px;
}

.shape-row {
  display: flex;
}

.shape-cell {
  width: 12px;
  height: 12px;
  margin: 1px;
}

.filled {
  background-color: #000;
}
</style>