<template>
  <uni-button
      :type="type"
      :loading="loading"
      @click="handleClick"
      class="common-btn"
  >
    <slot></slot> <!-- 插槽：自定义按钮文本 -->
  </uni-button>
</template>

<script setup>
const props = defineProps({
  type: { type: String, default: 'primary' }, // 按钮类型
  loading: { type: Boolean, default: false }  // 加载状态
});
const emit = defineEmits(['click']);
const handleClick = () => emit('click');
</script>

<style scoped>
.common-btn { margin: 10rpx 0; }
</style>