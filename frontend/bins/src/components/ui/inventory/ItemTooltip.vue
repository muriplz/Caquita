<template>
  <teleport to="body">
    <div class="item-tooltip" :style="tooltipStyle">
      <div class="tooltip-header">
        <h3>{{ item.name }}</h3>
        <span class="tooltip-rarity" :style="{ color: getRarityColor(item.rarity) }">{{ item.rarity }}</span>
      </div>
      <div class="tooltip-info">
        <div>ID: {{ item.itemId }}</div>
        <div>Size: {{ item.width }}x{{ item.height }}</div>
        <div v-if="item.properties">
          <div v-for="(value, key) in item.properties" :key="key">
            {{ key }}: {{ value }}
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script>
export default {
  name: 'ItemTooltip',

  props: {
    item: {
      type: Object,
      required: true
    },
    tooltipPosition: {
      type: Object,
      required: true
    }
  },

  computed: {
    tooltipStyle() {
      return {
        left: `${this.tooltipPosition.x}px`,
        top: `${this.tooltipPosition.y}px`
      };
    }
  },

  methods: {
    getRarityColor(rarity) {
      if (!rarity) return '#a0a0a0';

      const rarityColors = {
        junk: '#a0a0a0',
        common: 'rgb(246,193,52)',
        uncommon: '#44bd32',
        rare: '#0097e6',
        epic: '#8c7ae6',
        legendary: '#e1b12c'
      };

      return rarityColors[rarity.toLowerCase()] || '#a0a0a0';
    }
  }
};
</script>

<style scoped>
.item-tooltip {
  position: fixed;
  width: 200px;
  background-color: rgba(0, 0, 0, 0.85);
  color: white;
  border-radius: 4px;
  padding: 10px;
  z-index: 100000;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  pointer-events: none;
}

.tooltip-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 5px;
  margin-bottom: 5px;
}

.tooltip-header h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.tooltip-rarity {
  font-size: 12px;
  font-weight: bold;
}

.tooltip-info {
  font-size: 12px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
</style>