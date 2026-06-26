<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api, type Booking, type BookingStatus } from '@/api'

const list = ref<Booking[]>([])
const status = ref<BookingStatus | ''>('')
const loading = ref(false)

const statusName = (s: BookingStatus) =>
  ({ BOOKED: '已预约', CHECKED_IN: '已签到', COMPLETED: '已完成', CANCELLED: '已取消', NO_SHOW: '爽约' } as Record<BookingStatus, string>)[s] || s
const statusClass = (s: BookingStatus) =>
  ({ BOOKED: 'tag-primary', CHECKED_IN: 'tag-warning', COMPLETED: 'tag-success', CANCELLED: 'tag-info', NO_SHOW: 'tag-danger' } as Record<BookingStatus, string>)[s] || ''

const load = async () => {
  loading.value = true
  try {
    const r = await api.h5.bookings(status.value || undefined)
    list.value = r.list || []
  } catch { /* */ }
  finally { loading.value = false }
}
onShow(load)

const setStatus = (s: BookingStatus | '') => { status.value = s; load() }

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
      <view :class="['tab', !status && 'active']" @click="setStatus('')">全部</view>
      <view :class="['tab', status === 'BOOKED' && 'active']" @click="setStatus('BOOKED')">已预约</view>
      <view :class="['tab', status === 'COMPLETED' && 'active']" @click="setStatus('COMPLETED')">已完成</view>
      <view :class="['tab', status === 'CANCELLED' && 'active']" @click="setStatus('CANCELLED')">已取消</view>
    </view>

    <view v-if="loading" class="empty">加载中...</view>
    <view v-else-if="list.length === 0" class="empty">
      <view class="empty-icon">▦</view>
      <text>暂无预约</text>
    </view>
    <view v-for="b in list" :key="b.id" class="booking">
      <view class="row">
        <text class="no">{{ b.bookingNo }}</text>
        <text :class="['tag', statusClass(b.status)]">{{ statusName(b.status) }}</text>
      </view>
      <view class="meta">扣次: {{ b.costTimes }} 次</view>
      <view class="meta">预约时间: {{ b.bookedAt }}</view>
      <view v-if="b.status === 'BOOKED'" class="btn-ghost cancel" @click="onCancel(b.id)">取消预约</view>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.tabs {
  display: flex;
  background: #fff;
  padding: 0 8px;
  position: sticky;
  top: 0;
  z-index: 1;
  box-shadow: 0 1px 0 var(--border);
}
.tab {
  flex: 1;
  text-align: center;
  font-size: 14px;
  color: var(--text-2);
  padding: 12px 0;
  position: relative;
}
.tab.active {
  color: var(--brand);
  font-weight: 600;
}
.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  border-radius: 2px;
}

.booking {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, .03);
}
.row { display: flex; justify-content: space-between; align-items: center; }
.no { font-size: 14px; font-weight: 600; }
.meta { color: var(--text-3); font-size: 12px; margin-top: 6px; }
.cancel {
  margin-top: 12px;
  padding: 8px 0;
  text-align: center;
  border-radius: 16px;
  font-size: 13px;
}
</style>
