import { auth, clearAuth } from '@/store/auth'

const BASE_URL = '/api'
const LOGIN_URL = '/pages/login/login'

export interface ApiEnvelope<T> {
  code: number
  message?: string
  data: T
}

/**
 * 统一请求封装：自动带上 Token、统一处理 401、统一错误提示。
 */
export function request<T = unknown>(options: UniApp.RequestOptions): Promise<T> {
  const { url, header, ...rest } = options
  return new Promise<T>((resolve, reject) => {
    uni.request({
      ...rest,
      url: BASE_URL + url,
      header: {
        'Content-Type': 'application/json',
        ...(auth.token ? { Authorization: `Bearer ${auth.token}` } : {}),
        ...(header || {})
      },
      success: (res) => {
        const body = res.data as ApiEnvelope<T> | undefined
        if (body && body.code === 0) {
          resolve(body.data)
          return
        }
        if (body?.code === 401) {
          clearAuth()
          uni.reLaunch({ url: LOGIN_URL })
        }
        const msg = body?.message || '请求失败'
        uni.showToast({ title: msg, icon: 'none' })
        reject(body || new Error(msg))
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}
