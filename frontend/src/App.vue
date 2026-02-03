<template>
  <div class="app-container">
    <header>
      <h1>BetterGI 脚本工具集</h1>
      <p>后端版本：{{ '加载中...' }}</p>
    </header>

    <main>
      <router-view v-slot="{ Component }">
        <component :is="Component" />
      </router-view>
    </main>

    <footer>
      <p>Powered by Vue 3 + Spring Boot</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
// import { ref, onMounted } from 'vue'
// import axios from 'axios'
// import { API_BASE } from './utils/context'
//
// const backendVersion = ref('')
//
// onMounted(async () => {
//   try {
//     // 示例：调用后端一个 info 接口（你可自行在后端加）
//     const res = await axios.get(API_BASE + 'info')  // 或 /actuator/info
//     backendVersion.value = res.data?.version || '未知'
//   } catch (e) {
//     console.error('获取版本失败', e)
//   }
// })
</script>

<style scoped>
.app-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: system-ui, sans-serif;
}
header { text-align: center; margin-bottom: 2rem; }
footer { text-align: center; margin-top: 3rem; color: #666; }
</style>