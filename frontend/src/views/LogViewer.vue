<template>
  <div class="log-viewer">
    <!-- 控制栏 -->
    <div class="controls">
      <select v-model="logLevel" @change="filterLogs">
        <option value="ALL">全部</option>
        <option value="INFO">INFO</option>
        <option value="WARN">WARN</option>
        <option value="ERROR">ERROR</option>
      </select>
      <button @click="clearLogs">清空日志</button>
      <button @click="toggleAutoRefresh">
        {{ isAutoRefresh ? '停止刷新' : '开始刷新' }}
      </button>
    </div>

    <!-- 日志展示区域 -->
    <div class="log-container" ref="logContainer">
      <div
        v-for="(log, index) in filteredLogs"
        :key="index"
        :class="['log-item', log.level.toLowerCase()]"
      >
        [{{ log.timestamp }}] {{ log.level }}: {{ log.message }}
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';

export default {
  name: 'LogViewer',
  setup() {
    // 响应式数据
    const logs = ref([]); // 所有日志
    const filteredLogs = ref([]); // 过滤后的日志
    const logLevel = ref('ALL'); // 当前选择的日志级别
    const isAutoRefresh = ref(false); // 是否自动刷新
    const logContainer = ref(null); // 日志容器 DOM 引用

    // WebSocket 连接（假设后端提供日志推送服务）
    let socket = null;

    // 初始化 WebSocket
    const initWebSocket = () => {
      socket = new WebSocket('ws://localhost:8080/logs');
      socket.onmessage = (event) => {
        const newLog = JSON.parse(event.data);
        logs.value.push(newLog);
        filterLogs(); // 过滤日志
        scrollToBottom(); // 滚动到底部
      };
    };

    // 过滤日志
    const filterLogs = () => {
      if (logLevel.value === 'ALL') {
        filteredLogs.value = logs.value;
      } else {
        filteredLogs.value = logs.value.filter(
          (log) => log.level === logLevel.value
        );
      }
    };

    // 清空日志
    const clearLogs = () => {
      logs.value = [];
      filteredLogs.value = [];
    };

    // 切换自动刷新
    const toggleAutoRefresh = () => {
      isAutoRefresh.value = !isAutoRefresh.value;
      if (isAutoRefresh.value) {
        startPolling();
      } else {
        stopPolling();
      }
    };

    // 轮询获取日志（备用方案）
    let pollingInterval = null;
    const startPolling = () => {
      pollingInterval = setInterval(async () => {
        try {
          const response = await fetch('/api/logs');
          const newLogs = await response.json();
          logs.value.push(...newLogs);
          filterLogs();
          scrollToBottom();
        } catch (error) {
          console.error('获取日志失败:', error);
        }
      }, 3000); // 每3秒请求一次
    };

    const stopPolling = () => {
      if (pollingInterval) {
        clearInterval(pollingInterval);
        pollingInterval = null;
      }
    };

    // 滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        if (logContainer.value) {
          logContainer.value.scrollTop = logContainer.value.scrollHeight;
        }
      });
    };

    // 生命周期钩子
    onMounted(() => {
      initWebSocket(); // 初始化 WebSocket
    });

    onUnmounted(() => {
      if (socket) socket.close(); // 关闭 WebSocket
      stopPolling(); // 停止轮询
    });

    return {
      logs,
      filteredLogs,
      logLevel,
      isAutoRefresh,
      logContainer,
      filterLogs,
      clearLogs,
      toggleAutoRefresh,
    };
  },
};
</script>

<style scoped>
.log-viewer {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.controls {
  display: flex;
  gap: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #ddd;
}

.log-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  font-family: monospace;
  background-color: #000;
  color: #fff;
}

.log-item {
  margin-bottom: 5px;
  white-space: pre-wrap;
}

.info {
  color: #4caf50;
}

.warn {
  color: #ff9800;
}

.error {
  color: #f44336;
}
</style>
