// src/utils/context.js

// 優先使用後端注入的 window.CONTEXT_PATH（透過 <script src="/context.js"> 載入）
// 如果沒有被注入，則 fallback 到預設值（根據你的後端 context-path 調整）
const rawContextPath = window.CONTEXT_PATH || '/bgi/';

// 標準化：確保一定以 '/' 結尾
export const CONTEXT_PATH = rawContextPath.endsWith('/')
    ? rawContextPath
    : rawContextPath + '/';

// API 基底路徑
export const API_BASE = CONTEXT_PATH;

// 產生完整 API 網址的工具函式
export const getApiUrl = (endpoint) => {
    // 去掉開頭的 /（避免重複）
    const clean = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
    return `${API_BASE}${clean}`;
};