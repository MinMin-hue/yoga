import { reactive } from 'vue'

interface UserState {
  token: string
  profile: any
}

const state = reactive<UserState>({
  token: uni.getStorageSync('yoga_member_token') || '',
  profile: uni.getStorageSync('yoga_member_profile') || null
})

// 与原 pinia 接口保持一致：直接返回响应式 state（替代 useUserStore()）
export const useUserStore = () => state

export const setLogin = (token: string, profile: any) => {
  state.token = token
  state.profile = profile
  uni.setStorageSync('yoga_member_token', token)
  uni.setStorageSync('yoga_member_profile', profile)
}

export const clearLogin = () => {
  state.token = ''
  state.profile = null
  uni.removeStorageSync('yoga_member_token')
  uni.removeStorageSync('yoga_member_profile')
}
