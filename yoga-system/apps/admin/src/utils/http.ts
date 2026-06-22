import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('yoga_admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (resp) => {
    const data = resp.data
    if (data.code === 0) return data
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(data)
  },
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      ElMessage.warning('请重新登录')
      localStorage.removeItem('yoga_admin_token')
      router.push('/login')
    } else {
      ElMessage.error(err.response?.data?.message || err.message || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default http
