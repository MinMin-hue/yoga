<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '@/utils/request'
import { useUserStore } from '@/store/user'

const user = useUserStore()
const profile = ref<any>({})

const load = async () => {
  if (!user.token) return
  const r: any = await api.profile()
  profile.value = r.data
}
onShow(load)

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确认退出登录?',
    success: ({ confirm }) => {
      if (confirm) { user.clear(); uni.reLaunch({ url: '/pages/login/login' }) }
    }
  })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <view class="avatar">{{ profile.nickname?.[0] || '🧘' }}</view>
      <view class="info">
        <view class="name">{{ profile.nickname || '未登录' }}</view>
        <view class="phone">{{ profile.phone || '' }}</view>
      </view>
    </view>
    <view class="grid">
      <view class="grid-item" @click="uni.navigateTo({ url: '/pages/member/cards' })">
        <view class="num">💳</view>
        <text>我的会员卡</text>
      </view>
      <view class="grid-item" @click="uni.switchTab({ url: '/pages/booking/my' })">
        <view class="num">📋</view>
        <text>我的预约</text>
      </view>
      <view class="grid-item">
        <view class="num">📞</view>
        <text>联系客服</text>
      </view>
      <view class="grid-item">
        <view class="num">ℹ️</view>
        <text>关于我们</text>
      </view>
    </view>
    <view class="logout" @click="logout">退出登录</view>
  </view>
</template>

<style scoped>
.header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 36px 20px; color: #fff; display: flex; align-items: center; }
.avatar { width: 64px; height: 64px; border-radius: 50%; background: rgba(255,255,255,.2); display: flex; align-items: center; justify-content: center; font-size: 32px; margin-right: 16px; }
.name { font-size: 18px; font-weight: 600; }
.phone { opacity: .9; font-size: 13px; margin-top: 4px; }
.grid { background: #fff; margin: 12px; border-radius: 12px; padding: 16px 0; display: grid; grid-template-columns: repeat(4, 1fr); }
.grid-item { display: flex; flex-direction: column; align-items: center; }
.num { font-size: 28px; margin-bottom: 6px; }
.logout { margin: 24px 12px; background: #fff; border-radius: 12px; text-align: center; padding: 14px; color: #f5576c; }
</style>
