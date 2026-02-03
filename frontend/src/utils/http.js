import axios from 'axios'

// 动态 API 前缀
let API_PREFIX = '/'

// 初始化 API 前缀
export async function initApiPrefix() {
    if (window.API_PREFIX) {
        API_PREFIX = window.API_PREFIX
    } else {
        try {
            const res = await axios.get('/context/bgi-tools/prefix') // 后端接口返回前缀
            API_PREFIX = res.data.API_PREFIX || '/'
        } catch (err) {
            console.warn('获取后端前缀失败，使用默认 "/"')
            API_PREFIX = '/'
        }
    }
}

// axios 封装函数
export function request(path, options = {}) {
    return axios({
        url: `${API_PREFIX}${path}`,
        ...options
    })
}
// 示例 API
// export function getNodeList() {
//     return request('/node/list', { method: 'GET' })
// }
//
// export function addNode(data) {
//     return request('/node/add', { method: 'POST', data })
// }
