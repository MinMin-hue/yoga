<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/utils/request'

const date = ref(new Date().toISOString().slice(0, 10))
const types = ref<any[]>([])
const selType = ref<number | null>(null)
const list = ref<any[]>([])

const load = async () => {
  const r: any = await api.scheduleList(date.value, selType.value || undefined)
  list.value = r.data
}
const loadTypes = async () => {
  const r: any = await api.courseTypeList()
  types.value = r.data
}
onMounted(() => { loadTypes(); load() })

const goDetail = (id: number) => uni.navigateTo({ url: `/pages/schedule/detail?id=${id}` })
const formatTime = (s: string) => s ? s.substring(11, 16) : ''
const statusName = (s: string) => ({ SCHEDULED: '可预约', FINISHED: '已结束', CANCELLED: '已取消' } as any)[s] || s
const onDateChange = (e: any) => { date.value = e.detail.value; load() }
</script>

<template>
  <view class="page">
    <view class="filter">
      <picker mode="date" :value="date" @change="onDateChange">
        <view class="date-pick">📅 {{ date }}</view>
      </picker>
      <scroll-view scroll-x class="types">
        <view :class="['type-chip', !selType && 'active']" @click="selType = null; load()">全部</view>
        <view v-for="t in types" :key="t.id" :class="['type-chip', selType === t.id && 'active']" @click="selType = t.id; load()">{{ t.name }}</view>
      </scroll-view>
    </view>
    <view v-if="list.length === 0" class="empty">该日暂无课程</view>
    <view v-for="s in list" :key="s.id" class="schedule" @click="goDetail(s.id)">
      <view class="time-block">
        <text class="t">{{ formatTime(s.startTime) }}</text>
        <text class="dur">{{ Math.round((new Date(s.endTime).getTime() - new Date(s.startTime).getTime()) / 60000) }} 分钟</text>
      </view>
      <view class="info">
        <view class="name">{{ s.courseTypeName }}</view>
        <view class="meta">{{ s.coachName }} · {{ s.roomName || '教室待定' }}</view>
        <view class="cap">已约 {{ s.bookedCount }} / {{ s.capacity }}</view>
      </view>
      <view class="btn-primary book">预约</view>
    </view>
  </view>
</template>

<style scoped>
.page { padding-bottom: 24px; }
.filter { background: #fff; padding: 12px 16px; position: sticky; top: 0; z-index: 1; }
.date-pick { display: inline-block; background: #f0f0f0; padding: 6px 12px; border-radius: 16px; font-size: 13px; margin-bottom: 8px; }
.types { white-space: nowrap; }
.type-chip { display: inline-block; padding: 4px 12px; margin-right: 8px; background: #f0f0f0; border-radius: 12px; font-size: 13px; }
.type-chip.active { background: #667eea; color: #fff; }
.empty { text-align: center; color: #909399; padding: 48px; }
.schedule { background: #fff; margin: 12px; padding: 14px; border-radius: 12px; display: flex; align-items: center; }
.time-block { width: 64px; text-align: center; }
.t { display: block; font-size: 18px; font-weight: 600; color: #667eea; }
.dur { font-size: 11px; color: #909399; }
.info { flex: 1; margin-left: 12px; }
.name { font-size: 15px; font-weight: 500; }
.meta { font-size: 12px; color: #909399; margin: 4px 0; }
.cap { font-size: 12px; color: #f5576c; }
.book { padding: 6px 16px; font-size: 13px; border-radius: 16px; }
</style>
