<template>
  <div class="tool-card">
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <button @click="handleClick">{{ buttonText }}</button>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  description: String,
  buttonText: {
    type: String,
    default: '使用工具'
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style scoped>
.tool-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  margin: 12px 0;
  background: #fafafa;
  transition: all 0.2s;
}

.tool-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

button {
  margin-top: 12px;
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>