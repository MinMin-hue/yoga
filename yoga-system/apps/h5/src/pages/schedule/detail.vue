<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/utils/request'
import { useUserStore } from '@/store/user'

const user = useUserStore()
const schedule = ref<any>({})
const cards = ref<any[]>([])
const selCard = ref<number | null>(null)
const showSheet = ref(false)
const submitting = ref(false)

const load = async () => {
  const id = Number((uni.getCurrentPages().pop() as any).options.id)
  const r: any = await api.scheduleList(new Date().toISOString().slice(0, 10))
  schedule.value = (r.data as any[]).find((x: any) => x.id === id) || {}
  const c: any = await api.myCards()
  cards.value = (c.data.cards || []).filter((x: any) => x.status === 'ACTIVE')
}
onMounted(load)

const formatTime = (s: string) => s ? s.substring(11, 16) : ''
const formatDate = (s: string) => s ? s.replace('T', ' ').substring(0, 16) : ''

const onBook = async () => {
  if (!user.token) return uni.navigateTo({ url: '/pages/login/login' })
  if (cards.value.length === 0) return uni.showToast({ title: '您还没有可用的会员卡', icon: 'none' })
  showSheet.value = true
}
const doBook = async () => {
  if (!selCard.value) return uni.showToast({ title: '请选择会员卡', icon: 'none' })
  submitting.value = true
  try {
    await api.createBooking({ scheduleId: schedule.value.id, cardId: selCard.value })
    uni.showToast({ title: '预约成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 600)
  } finally { submitting.value = false; showSheet.value = false }
}
</script>

<template>
  <view class="page" v-if="schedule.id">
    <view class="hero">
      <view class="name">{{ schedule.courseTypeName }}</view>
      <view class="time">📅 {{ formatDate(schedule.startTime) }} - {{ formatTime(schedule.endTime) }}</view>
      <view class="meta">👨‍🏫 {{ schedule.coachName }} · 🏠 {{ schedule.roomName || '教室待定' }}</view>
      <view class="cap">已约 {{ schedule.bookedCount }} / {{ schedule.capacity }} 人</view>
    </view>

    <view class="section">
      <view class="row"><text>单次消耗</text><text>{{ schedule.bookedCount }} 次数</text></view>
      <view class="row"><text>签到时间</text><text>课程开始前 {{ schedule.checkinBefore }} 分钟内</text></view>
    </view>

    <view class="footer">
      <button class="btn-primary" @click="onBook">立即预约</button>
    </view>

    <view v-if="showSheet" class="mask" @click="showSheet = false">
      <view class="sheet" @click.stop>
        <view class="sheet-title">选择会员卡</view>
        <view v-for="c in cards" :key="c.id" :class="['card-opt', selCard === c.id && 'active']" @click="selCard = c.id">
          <view>{{ c.cardTypeName }} ({{ c.cardNo }})</view>
          <view class="muted">剩余 {{ c.remainTimes || '不限' }} 次 · 到期 {{ c.validTo?.substring(0, 10) || '永久' }}</view>
        </view>
        <button class="btn-primary" :loading="submitting" @click="doBook">确认预约</button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { padding-bottom: 80px; }
.hero { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: #fff; padding: 28px 16px; }
.name { font-size: 22px; font-weight: 600; }
.time, .meta, .cap { margin-top: 8px; opacity: .95; font-size: 13px; }
.section { background: #fff; margin: 12px; border-radius: 12px; padding: 4px 16px; }
.row { display: flex; justify-content: space-between; padding: 14px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.row:last-child { border-bottom: none; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; padding: 12px; background: #fff; }
button { height: 48px; line-height: 48px; border-radius: 24px; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); z-index: 99; display: flex; align-items: flex-end; }
.sheet { width: 100%; background: #fff; border-radius: 16px 16px 0 0; padding: 16px; max-height: 70vh; overflow-y: auto; }
.sheet-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.card-opt { padding: 12px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 8px; }
.card-opt.active { border-color: #667eea; background: #f0f3ff; }
.muted { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
