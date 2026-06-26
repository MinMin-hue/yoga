<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { auth, clearAuth } from '@/store/auth'
import { api, type UserInfo } from '@/api'

const profile = ref<UserInfo | null>(null)
const loading = ref(false)

const load = async () => {
  if (!auth.token) return
  loading.value = true
  try { profile.value = await api.h5.profile() } catch { /* */ }
  finally { loading.value = false }
}
onShow(load)

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确认退出登录?',
    success: ({ confirm }) => {
      if (!confirm) return
      clearAuth()
      uni.reLaunch({ url: '/pages/login/login' })
    }
  })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <view class="avatar">{{ profile?.nickname?.[0] || '灵' }}</view>
      <view class="info">
        <view class="name">{{ profile?.nickname || '未登录' }}</view>
        <view class="phone">{{ profile?.phone || '请先登录' }}</view>
      </view>
      <view v-if="auth.token" class="logout-btn" @click="logout">退出</view>
    </view>

    <view class="grid">
      <view class="grid-item" @click="uni.navigateTo({ url: '/pages/member/cards' })">
        <view class="grid-icon" style="background: #FFEAE3; color: #FF6B4A;">▦</view>
        <text>我的会员卡</text>
      </view>
      <view class="grid-item" @click="uni.switchTab({ url: '/pages/booking/my' })">
        <view class="grid-icon" style="background: #F0E8FF; color: #A06BFF;">▤</view>
        <text>我的预约</text>
      </view>
      <view class="grid-item" @click="uni.navigateTo({ url: '/pages/member/test' })">
        <view class="grid-icon" style="background: #DDF5F7; color: #00B8C4;">◐</view>
        <text>我的体测</text>
      </view>
      <view class="grid-item" @click="uni.navigateTo({ url: '/pages/checkin/index' })">
        <view class="grid-icon" style="background: #FFF1DA; color: #F5A623;">✓</view>
        <text>每日打卡</text>
      </view>
    </view>

    <view class="list">
      <view class="list-item" @click="uni.makePhoneCall({ phoneNumber: '400-888-8888' })">
        <view class="li-left">
          <view class="li-icon" style="background: #E6F0FF; color: #4A8DFF;">☎</view>
          <text>联系客服</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="list-item" @click="uni.showToast({ title: '灵伽瑜伽 v1.0.0', icon: 'none' })">
        <view class="li-left">
          <view class="li-icon" style="background: #F3F4F6; color: #6B7280;">i</view>
          <text>关于我们</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="list-item" @click="uni.showToast({ title: '已是最新版本', icon: 'none' })">
        <view class="li-left">
          <view class="li-icon" style="background: #E5F6EE; color: #23B573;">↑</view>
          <text>检查更新</text>
        </view>
        <text class="li-meta">v1.0.0</text>
        <text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.header {
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  padding: 36px 20px;
  color: #fff;
  display: flex;
  align-items: center;
  border-radius: 0 0 20px 20px;
  margin: -12px -12px 12px;
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, .25);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  margin-right: 16px;
  border: 2px solid rgba(255, 255, 255, .4);
}
.info { flex: 1; }
.name { font-size: 18px; font-weight: 600; }
.phone { opacity: .9; font-size: 13px; margin-top: 4px; }
.logout-btn {
  font-size: 12px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, .25);
  border-radius: 12px;
}

.grid {
  background: #fff;
  border-radius: 12px;
  padding: 20px 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  margin-bottom: 12px;
}
.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 12px;
  color: var(--text-2);
}
.grid-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 8px;
  font-weight: 700;
}

.list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
.list-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border);
}
.list-item:last-child { border-bottom: none; }
.li-left { flex: 1; display: flex; align-items: center; }
.li-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  margin-right: 12px;
}
.arrow { color: var(--text-3); font-size: 18px; }
.li-meta { color: var(--text-3); font-size: 12px; margin-right: 6px; }
</style>
