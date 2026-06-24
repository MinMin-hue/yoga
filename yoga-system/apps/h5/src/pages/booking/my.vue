<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api, type Booking, type BookingStatus } from '@/api'

const list = ref<Booking[]>([])
const status = ref<BookingStatus | ''>('')

const statusName = (s: BookingStatus) =>
  ({ BOOKED: '已预约', CHECKED_IN: '已签到', COMPLETED: '已完成', CANCELLED: '已取消', NO_SHOW: '爽约' } as Record<BookingStatus, string>)[s] || s
const statusType = (s: BookingStatus) =>
  ({ BOOKED: 'primary', CHECKED_IN: 'warning', COMPLETED: 'success', CANCELLED: 'info', NO_SHOW: 'danger' } as Record<BookingStatus, string>)[s] || ''

const load = async () => {
  try {
    const r = await api.h5.bookings(status.value || undefined)
    list.value = r.list || []
  } catch { /* */ }
}
onShow(load)

const onCancel = async (id: number) => {
  const { confirm } = await uni.showModal({ title: '提示', content: '确认取消该预约? 临近课程取消可能扣除次数' })
  if (!confirm) return
  try {
    await api.h5.cancelBooking(id, '用户主动取消')
    uni.showToast({ title: '已取消', icon: 'success' })
    load()
  } catch { /* */ }
}
</script>

<template>
  <view class="page">
    <view class="tabs">
      <view :class="['tab', !status && 'active']" @click="status = ''; load()">全部</view>
      <view :class="['tab', status === 'BOOKED' && 'active']" @click="status = 'BOOKED'; load()">已预约</view>
      <view :class="['tab', status === 'COMPLETED' && 'active']" @click="status = 'COMPLETED'; load()">已完成</view>
      <view :class="['tab', status === 'CANCELLED' && 'active']" @click="status = 'CANCELLED'; load()">已取消</view>
    </view>
    <view v-if="list.length === 0" class="empty">暂无预约</view>
    <view v-for="b in list" :key="b.id" class="booking">
      <view class="row">
        <text class="no">{{ b.bookingNo }}</text>
        <text :class="['status', `s-${statusType(b.status)}`]">{{ statusName(b.status) }}</text>
      </view>
      <view class="meta">扣次: {{ b.costTimes }} 次</view>
      <view class="meta">预约时间: {{ b.bookedAt }}</view>
      <view v-if="b.status === 'BOOKED'" class="btn-primary cancel" @click="onCancel(b.id)">取消预约</view>
    </view>
  </view>
</template>

<style scoped>
.page { padding-bottom: 24px; }
.tabs { display: flex; background: #fff; padding: 12px 16px; position: sticky; top: 0; z-index: 1; }
.tab { flex: 1; text-align: center; font-size: 14px; color: #606266; padding: 6px 0; }
.tab.active { color: #667eea; font-weight: 600; border-bottom: 2px solid #667eea; }
.empty { text-align: center; color: #909399; padding: 48px; }
.booking { background: #fff; margin: 12px; padding: 16px; border-radius: 12px; }
.row { display: flex; justify-content: space-between; align-items: center; }
.no { font-size: 14px; font-weight: 500; }
.status { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.s-primary { color: #667eea; background: #e0e7ff; }
.s-warning { color: #f59e0b; background: #fef3c7; }
.s-success { color: #10b981; background: #d1fae5; }
.s-info { color: #6b7280; background: #f3f4f6; }
.s-danger { color: #ef4444; background: #fee2e2; }
.meta { color: #909399; font-size: 12px; margin-top: 6px; }
.cancel { margin-top: 12px; padding: 8px 0; text-align: center; border-radius: 16px; font-size: 13px; }
</style>
