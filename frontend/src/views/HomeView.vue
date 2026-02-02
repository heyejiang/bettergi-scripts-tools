<template>
  <div class="home">
    <h2>欢迎使用扩展工具</h2>

    <section>
      <h3>测试 Cron 解析</h3>
      <input v-model="cronExpr" placeholder="输入 cron 表达式，如 0 0 12 * * ?" />
      <button @click="fetchNextTime" :disabled="loading">获取下次执行时间</button>
      <pre v-if="result">{{ result }}</pre>
      <p v-if="error" style="color: red;">{{ error }}</p>
    </section>

    <section>
      <h3>可用工具</h3>
      <ul>
        <li><a href="#">WebSocket 代理测试</a> (待实现)</li>
        <li><a href="#">OCR 字节识别</a> (待实现)</li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { getApiUrl } from '@/utils/context'

const cronExpr = ref('0 0 12 * * ?')
const result = ref('')
const error = ref('')
const loading = ref(false)

const fetchNextTime = async () => {
  loading.value = true
  error.value = ''
  result.value = ''

  try {
    const res = await axios.get(getApiUrl('cron/next-timestamp'), {
      params: { expression: cronExpr.value }
    })
    result.value = JSON.stringify(res.data, null, 2)
  } catch (e: any) {
    error.value = e.message || '请求失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.home { padding: 1rem; }
section { margin: 2rem 0; padding: 1rem; border: 1px solid #ddd; border-radius: 8px; }
input { padding: 8px; width: 300px; margin-right: 10px; }
button { padding: 8px 16px; }
pre { background: #f4f4f4; padding: 1rem; border-radius: 4px; overflow-x: auto; }
</style>