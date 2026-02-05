<template>
  <div class="container">
    <h1 class="title">BetterGI Script Tools</h1>

    <!-- Cron 相关功能 -->
    <div class="section">
      <div class="card">
        <h2 class="section-title">Cron 功能</h2>
        <div class="form-group">
          <label class="label">Cron 表达式:</label>
          <input v-model="cronExpression" class="input" placeholder="例如: 0 0 * * * ?" />
        </div>
        <div class="form-group">
          <label class="label">开始时间戳:</label>
          <input v-model.number="startTimestamp" class="input" type="number" placeholder="开始时间戳" />
        </div>
        <div class="form-group">
          <label class="label">结束时间戳:</label>
          <input v-model.number="endTimestamp" class="input" type="number" placeholder="结束时间戳" />
        </div>
        <button @click="getNextTimestamp" class="btn primary">获取下一个时间戳</button>
        <pre class="result">{{ cronResult }}</pre>
      </div>

      <div class="card">
        <h2 class="section-title">Cron 任务列表</h2>
        <div v-for="(item, index) in cronList" :key="index" class="cron-item">
          <input v-model="item.key" class="input small" placeholder="任务标识唯一值" />
          <input v-model="item.cronExpression" class="input small" placeholder="Cron 表达式 如: 0 0 * * * ?" />
          <input v-model.number="item.startTimestamp" class="input small" type="number" placeholder="开始时间戳" />
          <input v-model.number="item.endTimestamp" class="input small" type="number" placeholder="结束时间戳" />
          <button @click="cronListRemoveItem(index)" class="btn danger">删除</button>
        </div>
        <div class="actions">
          <button @click="cronListAddItem" class="btn secondary">添加任务</button>
          <button @click="cronListSubmit" class="btn primary">提交</button>
        </div>
        <pre class="result">{{ cronListResult }}</pre>
      </div>
    </div>

    <!-- OCR 相关功能 -->
    <div class="section">
      <div class="card">
        <h2 class="section-title">OCR 功能</h2>
        <input type="file" @change="handleFileUpload" class="file-input" />
        <button @click="performOcr" class="btn primary">执行 OCR 识别</button>
        <pre class="result">{{ ocrResult }}</pre>
      </div>
    </div>
  </div>
</template>


<script>
import {ref} from 'vue'
import service from "@utils/request";

export default {
  name: 'CapabilitiesView',
  setup() {
    const cronResult = ref('')
    const ocrResult = ref('')
    const file = ref(null)
    const cronExpression = ref('')
    // const timeRange = ref([])
    const startTimestamp = ref(Date.now())
    const endTimestamp = ref(Date.now() + 86400000)
    const cronList = ref([
      {
        key: 'task1',
        cronExpression: '0 0 * * * ?',
        startTimestamp: Date.now(),
        endTimestamp: Date.now() + 86400000,
      },
    ]);
    const cronListResult = ref('');

    const cronListAddItem = () => {
      cronList.value.push({
        key: `task${cronList.value.length + 1}`,
        cronExpression: '',
        startTimestamp: Date.now(),
        endTimestamp: Date.now() + 86400000,
      });
    };

    const cronListRemoveItem = (index) => {
      cronList.value.splice(index, 1);
    };

    const cronListSubmit = async () => {
      try {
        const response = await service.post('/cron/next-timestamp/all', {
          cronList: cronList.value,
        });
        cronListResult.value = JSON.stringify(response.data, null, 2);
      } catch (error) {
        console.error('请求失败:', error);
      }
    };

    // 快捷选项
    const shortcuts = [
      {
        text: '最近一周',
        value: () => {
          const end = new Date()
          const start = new Date()
          start.setDate(start.getDate() - 7)
          return [start, end]
        },
      },
      {
        text: '最近一个月',
        value: () => {
          const end = new Date()
          const start = new Date()
          start.setMonth(start.getMonth() - 1)
          return [start, end]
        },
      },
      {
        text: '最近三个月',
        value: () => {
          const end = new Date()
          const start = new Date()
          start.setMonth(start.getMonth() - 3)
          return [start, end]
        },
      },
    ]

    // 获取单个 Cron 表达式的下一个时间戳
    const getNextTimestamp = async () => {
      try {
        // const [start, end] = timeRange.value || [new Date(), new Date(Date.now() + 86400000)]
        const response = await service.post("/cron/next-timestamp", {
          cronExpression: cronExpression.value,
          startTimestamp: startTimestamp.value,
          endTimestamp: endTimestamp.value,
        })
        cronResult.value = JSON.stringify(response.data, null, 2)
      } catch (error) {
        console.error('Error fetching next timestamp:', error)
      }
    }

    // 处理文件上传
    const handleFileUpload = (event) => {
      file.value = event.target.files[0]
    }

    // 执行 OCR 识别
    const performOcr = async () => {
      if (!file.value) {
        alert('请先选择一个文件')
        return
      }

      try {
        const reader = new FileReader()
        reader.onload = async (e) => {
          const arrayBuffer = e.target.result
          const bytes = Array.from(new Uint8Array(arrayBuffer))

          const response = await service.post('/ocr/bytes', {
            bytes: bytes,
          })

          ocrResult.value = JSON.stringify(response.data, null, 2)
        }

        reader.readAsArrayBuffer(file.value)
      } catch (error) {
        console.error('Error performing OCR:', error)
      }
    }

    return {
      cronResult,
      ocrResult,
      file,
      cronExpression,
      // timeRange,
      startTimestamp,
      endTimestamp,
      shortcuts,
      getNextTimestamp,
      cronList,
      cronListResult,
      cronListAddItem,
      cronListRemoveItem,
      cronListSubmit,
      handleFileUpload,
      performOcr,
    }
  },
}
</script>


<style scoped>
pre {
  background-color: #f4f4f4;
  padding: 10px;
  border-radius: 5px;
  white-space: pre-wrap;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.section-title {
  font-size: 1.5em;
  margin-bottom: 15px;
  color: #444;
}

.form-group {
  margin-bottom: 15px;
}

.label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1em;
}

.input:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 4px rgba(0, 123, 255, 0.3);
}

.small {
  width: calc(100% - 120px);
  display: inline-block;
  margin-right: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1em;
  transition: background 0.3s ease;
}

.primary {
  background: #007bff;
  color: #fff;
}

.primary:hover {
  background: #0056b3;
}

.secondary {
  background: #6c757d;
  color: #fff;
}

.secondary:hover {
  background: #5a6268;
}

.danger {
  background: #dc3545;
  color: #fff;
}

.danger:hover {
  background: #bd2130;
}

.actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.file-input {
  margin-bottom: 15px;
}

.result {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-wrap;
  margin-top: 15px;
}
</style>