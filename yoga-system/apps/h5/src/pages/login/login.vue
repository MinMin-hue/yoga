<script setup lang="ts">
import { ref } from 'vue'
import api from '@/utils/request'
import { useUserStore } from '@/store/user'

const phone = ref('')
const loading = ref(false)
const user = useUserStore()

const onLogin = async () => {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    return uni.showToast({ title: '请输入正确手机号', icon: 'none' })
  }
  loading.value = true
  try {
    const r: any = await api.memberLogin({ phone: phone.value })
    user.setLogin(r.data.token, r.data.profile)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 600)
  } finally { loading.value = false }
}
</script>

<template>
  <view class="login">
    <view class="logo">🧘</view>
    <view class="title">瑜伽馆</view>
    <view class="subtitle">专业瑜伽, 健康生活</view>
    <view class="form">
      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="phone" type="number" maxlength="11" placeholder="请输入手机号登录" />
      </view>
      <button class="btn-primary" :loading="loading" @click="onLogin">登 录</button>
      <view class="tip">首次登录将自动注册</view>
    </view>
  </view>
</template>

<style scoped>
.login { padding: 80px 24px 24px; }
.logo { font-size: 80px; text-align: center; margin-bottom: 12px; }
.title { text-align: center; font-size: 24px; font-weight: 600; }
.subtitle { text-align: center; color: #909399; margin-bottom: 36px; }
.form-item { background: #fff; border-radius: 8px; padding: 14px; margin-bottom: 16px; display: flex; align-items: center; }
.label { width: 70px; color: #606266; }
input { flex: 1; font-size: 16px; }
button { width: 100%; height: 48px; line-height: 48px; border-radius: 24px; }
.tip { text-align: center; color: #909399; font-size: 12px; margin-top: 16px; }
</style>
