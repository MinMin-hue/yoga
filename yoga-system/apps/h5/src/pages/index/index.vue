<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import api from '@/utils/request'
import { useUserStore } from '@/store/user'

const user = useUserStore()
const banners = [
  { id: 1, title: '专业瑜伽课程', desc: '适合初学者到进阶者', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, title: '会员卡优惠', desc: '月卡 599 元起, 不限次数', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 3, title: '私教 1对1', desc: '专业教练个性化指导', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
]
const cardTypes = ref<any[]>([])
const today = new Date().toISOString().slice(0, 10)
const schedules = ref<any[]>([])
const courseTypes = ref<any[]>([])

const load = async () => {
  const r: any = await api.cardTypeList(); cardTypes.value = r.data
  const s: any = await api.scheduleList(today); schedules.value = (s.data || []).slice(0, 5)
  const c: any = await api.courseTypeList(); courseTypes.value = c.data
}

onShow(() => { if (!user.token) uni.reLaunch({ url: '/pages/login/login' }) })
onMounted(load)
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh() })

const goCard = () => uni.switchTab({ url: '/pages/card/list' })
const goSchedule = () => uni.navigateTo({ url: '/pages/schedule/list' })
const goBuy = (id: number) => uni.navigateTo({ url: `/pages/card/buy?id=${id}` })
const goScheduleDetail = (id: number) => uni.navigateTo({ url: `/pages/schedule/detail?id=${id}` })

const kindName = (k: string) => ({ TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' } as any)[k] || k
const formatTime = (s: string) => s ? s.substring(11, 16) : ''
</script>

<template>
  <view class="page">
    <view class="banner" v-for="b in banners" :key="b.id" :style="{ background: b.color }">
      <view class="banner-title">{{ b.title }}</view>
      <view class="banner-desc">{{ b.desc }}</view>
    </view>

    <view class="quick-grid">
      <view class="quick-item" @click="goCard">
        <view class="quick-icon">💳</view><text>购卡</text>
      </view>
      <view class="quick-item" @click="goSchedule">
        <view class="quick-icon">📅</view><text>约课</text>
      </view>
      <view class="quick-item" @click="goCard">
        <view class="quick-icon">🎓</view><text>私教</text>
      </view>
      <view class="quick-item" @click="goSchedule">
        <view class="quick-icon">📍</view><text>门店</text>
      </view>
    </view>

    <view class="section">
      <view class="section-title">热门会员卡</view>
      <view v-for="c in cardTypes" :key="c.id" class="card-item" @click="goBuy(c.id)">
        <view class="card-row">
          <text class="card-name">{{ c.name }}</text>
          <text class="card-kind">{{ kindName(c.cardKind) }}</text>
        </view>
        <view class="card-price">¥{{ c.price }}</view>
        <view class="card-desc">{{ c.description }}</view>
      </view>
    </view>

    <view class="section">
      <view class="section-title">今日课程</view>
      <view v-if="schedules.length === 0" class="empty">今日暂无课程</view>
      <view v-for="s in schedules" :key="s.id" class="schedule-item" @click="goScheduleDetail(s.id)">
        <view class="time">{{ formatTime(s.startTime) }}</view>
        <view class="info">
          <view class="name">{{ s.courseTypeName }}</view>
          <view class="meta">{{ s.coachName }} · {{ s.roomName || '教室待定' }} · {{ s.bookedCount }}/{{ s.capacity }}</view>
        </view>
        <view class="btn-primary book">预约</view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { padding-bottom: 24px; }
.banner { margin: 12px; padding: 24px; border-radius: 16px; color: #fff; }
.banner-title { font-size: 22px; font-weight: 600; }
.banner-desc { font-size: 13px; opacity: .9; margin-top: 6px; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); background: #fff; margin: 12px; border-radius: 12px; padding: 16px 0; }
.quick-item { display: flex; flex-direction: column; align-items: center; }
.quick-icon { font-size: 28px; margin-bottom: 6px; }
.section { background: #fff; margin: 12px; border-radius: 12px; padding: 12px 0; }
.section-title { font-size: 16px; font-weight: 600; padding: 0 16px 12px; }
.card-item { padding: 12px 16px; border-bottom: 1px solid #f5f5f5; }
.card-row { display: flex; justify-content: space-between; align-items: center; }
.card-name { font-weight: 600; }
.card-kind { font-size: 12px; color: #909399; background: #f0f0f0; padding: 2px 8px; border-radius: 4px; }
.card-price { color: #f5576c; font-size: 20px; font-weight: 600; margin: 6px 0; }
.card-desc { color: #909399; font-size: 12px; }
.schedule-item { display: flex; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f5f5f5; }
.time { font-size: 18px; font-weight: 600; color: #667eea; margin-right: 12px; min-width: 56px; }
.info { flex: 1; }
.name { font-size: 15px; font-weight: 500; }
.meta { color: #909399; font-size: 12px; margin-top: 4px; }
.book { padding: 6px 14px; font-size: 13px; }
.empty { text-align: center; color: #909399; padding: 24px; }
</style>
