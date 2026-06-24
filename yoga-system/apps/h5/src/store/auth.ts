import { reactive } from 'vue'

export interface UserInfo {
  id: number
  nickname: string
  phone: string
  avatar?: string
  [key: string]: unknown
}

interface AuthState {
  token: string
  user: UserInfo | null
}

const STORAGE_KEY = 'yoga_auth'

function load(): AuthState {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY) as AuthState | null
    if (raw && typeof raw === 'object') {
      return { token: raw.token || '', user: raw.user || null }
    }
  } catch {
    /* ignore */
  }
  return { token: '', user: null }
}

// 单例响应式状态：整个应用共享
export const auth = reactive<AuthState>(load())

export function setAuth(token: string, user: UserInfo): void {
  auth.token = token
  auth.user = user
  uni.setStorageSync(STORAGE_KEY, { token, user })
}

export function clearAuth(): void {
  auth.token = ''
  auth.user = null
  uni.removeStorageSync(STORAGE_KEY)
}

export const isLoggedIn = (): boolean => !!auth.token
