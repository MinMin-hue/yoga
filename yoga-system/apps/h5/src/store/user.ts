import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('yoga_member_token') || '',
    profile: uni.getStorageSync('yoga_member_profile') || null
  }),
  actions: {
    setLogin(token: string, profile: any) {
      this.token = token
      this.profile = profile
      uni.setStorageSync('yoga_member_token', token)
      uni.setStorageSync('yoga_member_profile', profile)
    },
    clear() {
      this.token = ''
      this.profile = null
      uni.removeStorageSync('yoga_member_token')
      uni.removeStorageSync('yoga_member_profile')
    }
  }
})
