import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from './router'

const api = axios.create({ baseURL: '/api' })

// 请求拦截器：自动加token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：401跳登录
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
      ElMessage.error('请先登录')
    }
    return Promise.reject(error)
  }
)

// Auth
export const register = (username, email, password) =>
  api.post('/register', { username, email, password })

export const login = (email, password) =>
  api.post('/login', { email, password })

export const getMe = () =>
  api.get('/me')

export const updateDomain = (customDomain) =>
  api.put('/me/domain', { customDomain })

// Short URL
export const shorten = (url, customCode, expireDays) =>
  api.post('/shorten', { url, customCode, expireDays })

export const batchShorten = (urls) =>
  api.post('/batch-shorten', { urls })

export const getStats = (shortCode) =>
  api.get(`/stats/${shortCode}`)

export const getMyLinks = () =>
  api.get('/my/links')

// Analytics
export const getAnalytics = (shortCode, days = 30) =>
  api.get(`/analytics/${shortCode}?days=${days}`)

// Safe Check
export const checkSafe = (url) =>
  api.get('/check-safe', { params: { url } })
