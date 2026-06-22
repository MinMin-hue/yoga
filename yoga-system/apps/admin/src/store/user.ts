import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminLogin } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('yoga_admin_token') || '')
  const profile = ref<any>(JSON.parse(localStorage.getItem('yoga_admin_profile') || 'null'))

  async function login(form: { username: string; password: string }) {
    const resp: any = await adminLogin(form)
    token.value = resp.data.token
    profile.value = resp.data.profile
    localStorage.setItem('yoga_admin_token', token.value)
    localStorage.setItem('yoga_admin_profile', JSON.stringify(profile.value))
    return resp
  }

  function logout() {
    token.value = ''
    profile.value = null
    localStorage.removeItem('yoga_admin_token')
    localStorage.removeItem('yoga_admin_profile')
  }

  return { token, profile, login, logout }
})
