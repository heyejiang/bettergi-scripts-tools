// src/utils/context.js

// 后端注入的 window.CONTEXT_PATH（如 '/bgi/'），fallback 到 '/'
export const CONTEXT_PATH = window.CONTEXT_PATH || '/'

// 统一 API 前缀（确保带 / 结尾）
export const API_BASE = CONTEXT_PATH.endsWith('/')
    ? CONTEXT_PATH
    : CONTEXT_PATH + '/'

// 工具函数：生成完整 API URL
export const getApiUrl = (endpoint) => {
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint
    return `${API_BASE}${cleanEndpoint}`
}