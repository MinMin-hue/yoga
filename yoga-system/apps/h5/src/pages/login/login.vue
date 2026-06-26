<script setup lang="ts">
import { ref } from 'vue'
import { auth, setAuth } from '@/store/auth'
import { api } from '@/api'

const phone = ref('')
const agree = ref(false)
const loading = ref(false)

const onLogin = async () => {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    return uni.showToast({ title: '请输入正确手机号', icon: 'none' })
  }
  if (!agree.value) {
    return uni.showToast({ title: '请先勾选服务协议', icon: 'none' })
  }
  loading.value = true
  try {
    const { token, profile } = await api.auth.login(phone.value)
    setAuth(token, profile)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 600)
  } catch {
    /* request 内部已 toast */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="login">
    <view class="brand">
      <view class="brand-mark">灵</view>
      <view class="brand-name">灵伽瑜伽</view>
      <view class="brand-sub">专业瑜伽 · 健康生活</view>
    </view>

    <view class="form">
      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="phone" type="number" maxlength="11" placeholder="请输入手机号登录" />
      </view>
      <button class="btn-primary btn-block" :loading="loading" :disabled="loading" @click="onLogin">登 录</button>

      <view class="agree" @click="agree = !agree">
        <view class="checkbox" :class="{ on: agree }">
          <text v-if="agree">✓</text>
        </view>
        <text class="agree-text">
          我已阅读并同意
          <text class="link">《用户服务协议》</text>
          <text class="link">《隐私政策》</text>
        </text>
      </view>
      <view class="tip">首次登录将自动注册账号</view>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.login { padding: 80px 24px 24px; }

.brand {
  text-align: center;
  margin-bottom: 48px;
}
.brand-mark {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(255, 107, 61, .25);
}
.brand-name {
  font-size: 22px;
  font-weight: 700;
  color: #1F1F1F;
}
.brand-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}

.form-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .03);
}
.label {
  width: 70px;
  color: #5B5B66;
  font-size: 14px;
}
input { flex: 1; font-size: 16px; color: #1F1F1F; }

.agree {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
}
.checkbox {
  width: 16px;
  height: 16px;
  border: 1.5px solid #DDD;
  border-radius: 50%;
  margin-right: 6px;
  font-size: 10px;
  color: #fff;
  text-align: center;
  line-height: 13px;
  background: #fff;
}
.checkbox.on {
  background: #FF6B3D;
  border-color: #FF6B3D;
}
.link { color: #FF6B3D; }
.tip { text-align: center; color: #909399; font-size: 12px; margin-top: 16px; }
</style>
