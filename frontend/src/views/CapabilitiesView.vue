<template>
  <div>
    <h1>BetterGI Script Tools</h1>


    <!-- Cron 相关功能 -->
    <div>
      <div class="cron-item-component">
        <h2>Cron 功能</h2>
        <label>
          Cron 表达式:
          <input v-model="cronExpression" placeholder="例如: 0 0 * * * ?"/>
        </label>
        <br/>
<!--          时间范围:-->
<!--          <el-date-picker-->
<!--              v-model="timeRange"-->
<!--              type="datetimerange"-->
<!--              range-separator="至"-->
<!--              start-placeholder="开始时间"-->
<!--              end-placeholder="结束时间"-->
<!--              :shortcuts="shortcuts"-->
<!--          />-->
        <input v-model.number="startTimestamp" type="number" placeholder="开始时间戳"/>
        <input v-model.number="endTimestamp" type="number" placeholder="结束时间戳"/>
        <br/>
        <button @click="getNextTimestamp">获取下一个时间戳</button>
        <pre>{{ cronResult }}</pre>
      </div>
      <div class="cron-list-component">
        <h2>Cron 任务列表</h2>
        <div v-for="(item, index) in cronList" :key="index" class="cron-item">
          <input v-model="item.key" placeholder="任务标识唯一值"/>
          <input v-model="item.cronExpression" placeholder="Cron 表达式 如: 0 0 * * * ?"/>
          <input v-model.number="item.startTimestamp" type="number" placeholder="开始时间戳"/>
          <input v-model.number="item.endTimestamp" type="number" placeholder="结束时间戳"/>
<!--          <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              :shortcuts="shortcuts"
          />-->
          <button @click="cronListRemoveItem(index)">删除</button>
        </div>
        <button @click="cronListAddItem">添加任务</button>
        <button @click="cronListSubmit">提交</button>
        <pre>{{ cronListResult }}</pre>
      </div>
    </div>


    <!-- OCR 相关功能 -->
    <div>
      <h2>OCR 功能</h2>
      <input type="file" @change="handleFileUpload"/>
      <button @click="performOcr">执行 OCR 识别</button>
      <pre>{{ ocrResult }}</pre>
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
      // {
      //   key: 'task1',
      //   cronExpression: '0 0 * * * ?',
      //   startTimestamp: Date.now(),
      //   endTimestamp: Date.now() + 86400000,
      // },
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
</style>
