<!-- src/App.vue（确保路由出口正常） -->
<template>
  <view class="app-container">
    <router-view />
  </view>
</template>

<script setup></script>

<style scoped>
@import "uview-plus/index.scss";
.app-container {
  min-height: 100vh;
  background-color: #ffffff;
}
</style>
