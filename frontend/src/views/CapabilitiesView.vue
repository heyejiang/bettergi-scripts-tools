<template xmlns="http://www.w3.org/1999/html">
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
        <label class="label">返回结果:</label>
        <pre class="result">{{ cronResult }}</pre>
      </div>

      <div class="card">
        <h2 class="section-title">Cron 任务列表</h2>
        <div v-for="(item, index) in cronList" :key="index" class="cron-item">
          <p>{{ index }}</p >
          <input v-model="item.key" class="input small" placeholder="任务标识唯一值" />
          <input v-model="item.cronExpression" class="input small" placeholder="Cron 表达式 如: 0 0 * * * ?" />
          <input v-model.number="item.startTimestamp" class="input small" type="number" placeholder="开始时间戳" />
          <input v-model.number="item.endTimestamp" class="input small" type="number" placeholder="结束时间戳" />
          <button @click="cronListRemoveItem(index)" class="btn danger">删除</button>
          <br/>
        </div>
        <div class="actions">
          <button @click="cronListAddItem" class="btn secondary">添加任务</button>
          <button @click="cronListSubmit" class="btn primary">提交</button>
        </div>
        <label class="label">返回结果:</label>
        <pre class="result">{{ cronListResult }}</pre>
      </div>
    </div>

    <!-- OCR 相关功能 -->
    <div class="section">
      <div class="card">
        <h2 class="section-title">OCR 功能</h2>
        <input type="file" @change="handleFileUpload" class="file-input" />
        <button @click="performOcr" class="btn primary">执行 OCR 识别</button>
        <label class="label">返回结果:</label>
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
/* 容器布局 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa, #e4edf9);
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 30px;
  color: #2c3e50;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 卡片样式 */
.card {
  background: #ffffff;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: #3498db;
  text-align: center;
}

/* 表单项 */
.form-group {
  margin-bottom: 20px;
}

.label {
  display: block;
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 8px;
  color: #2c3e50;
}

.input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

.input.small {
  width: calc(100% - 100px);
  display: inline-block;
  margin-right: 10px;
}

/* 按钮样式 */
.btn {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-right: 10px;
}

.btn.primary {
  background: #3498db;
  color: white;
}

.btn.primary:hover {
  background: #2980b9;
  transform: scale(1.05);
}

.btn.secondary {
  background: #95a5a6;
  color: white;
}

.btn.secondary:hover {
  background: #7f8c8d;
  transform: scale(1.05);
}

.btn.danger {
  background: #e74c3c;
  color: white;
}

.btn.danger:hover {
  background: #c0392b;
  transform: scale(1.05);
}

/* 文件上传 */
.file-input {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px dashed #3498db;
  border-radius: 8px;
  width: 100%;
  cursor: pointer;
}

.file-input:hover {
  background: #f1f8ff;
}

/* 结果展示 */
.result {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef); /* 添加渐变背景 */
  padding: 15px;
  border-radius: 8px;
  margin-top: 15px;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 0.9rem;
  color: #2c3e50;
}

/* 列表项 */
.cron-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  background: #fafafa;
}

.actions {
  text-align: center;
  margin-top: 20px;
}


</style>