<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type Schedule, type CourseType, type ScheduleStatus } from '@/api'
import { today, formatTime, durationMinutes } from '@/utils/format'

const date = ref(today())
const types = ref<CourseType[]>([])
const selType = ref<number | null>(null)
const list = ref<Schedule[]>([])
const loading = ref(false)

const statusName = (s?: ScheduleStatus) =>
  ({ SCHEDULED: '可预约', FULL: '已满', FINISHED: '已结束', CANCELLED: '已取消' } as Record<ScheduleStatus, string>)[s || 'SCHEDULED'] || s || ''
const statusClass = (s?: ScheduleStatus) =>
  ({ SCHEDULED: 'tag-primary', FULL: 'tag-warning', FINISHED: 'tag-info', CANCELLED: 'tag-danger' } as Record<ScheduleStatus, string>)[s || 'SCHEDULED'] || ''

const loadTypes = async () => {
  try { types.value = await api.h5.courseTypes() } catch { /* */ }
}
const load = async () => {
  loading.value = true
  try { list.value = await api.h5.schedules(date.value, selType.value ?? undefined) } catch { /* */ }
  finally { loading.value = false }
}
onMounted(() => { loadTypes(); load() })

const goDetail = (id: number) => uni.navigateTo({ url: `/pages/schedule/detail?id=${id}` })
const onDateChange = (e: { detail: { value: string } }) => { date.value = e.detail.value; load() }
const pickType = (id: number | null) => { selType.value = id; load() }
</script>

<template>
  <view class="page">
    <view class="filter">
      <picker mode="date" :value="date" @change="onDateChange">
        <view class="date-pick">▤ {{ date }}</view>
      </picker>
      <scroll-view scroll-x class="types">
        <view :class="['type-chip', !selType && 'active']" @click="pickType(null)">全部</view>
        <view
          v-for="t in types"
          :key="t.id"
          :class="['type-chip', selType === t.id && 'active']"
          @click="pickType(t.id)"
        >{{ t.name }}</view>
      </scroll-view>
    </view>

    <view v-if="loading" class="empty">加载中...</view>
    <view v-else-if="list.length === 0" class="empty">
      <view class="empty-icon">▦</view>
      <text>该日暂无课程</text>
    </view>
    <view
      v-for="s in list"
      :key="s.id"
      class="schedule"
      @click="goDetail(s.id)"
    >
      <view class="time-block">
        <text class="t">{{ formatTime(s.startTime) }}</text>
        <text class="dur">{{ durationMinutes(s.startTime, s.endTime) }} 分钟</text>
      </view>
      <view class="info">
        <view class="name-row">
          <text class="name">{{ s.courseTypeName }}</text>
          <text :class="['tag', statusClass(s.status)]">{{ statusName(s.status) }}</text>
        </view>
        <view class="meta">教练 {{ s.coachName }} · {{ s.roomName || '教室待定' }}</view>
        <view class="cap">已约 {{ s.bookedCount }} / {{ s.capacity }}</view>
      </view>
      <view class="btn-primary book">预约</view>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.filter {
  background: #fff;
  padding: 12px 16px;
  position: sticky;
  top: 0;
  z-index: 1;
  box-shadow: 0 1px 0 var(--border);
}
.date-pick {
  display: inline-block;
  background: var(--brand-bg);
  color: var(--brand);
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  margin-bottom: 10px;
  font-weight: 500;
}
.types { white-space: nowrap; }
.type-chip {
  display: inline-block;
  padding: 5px 14px;
  margin-right: 8px;
  background: #F5F5F7;
  color: #5B5B66;
  border-radius: 14px;
  font-size: 13px;
}
.type-chip.active {
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  color: #fff;
  font-weight: 500;
}

.schedule {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, .03);
}
.time-block {
  width: 64px;
  text-align: center;
  border-right: 1px dashed var(--border);
  padding-right: 8px;
}
.t { display: block; font-size: 18px; font-weight: 700; color: var(--brand); }
.dur { font-size: 11px; color: var(--text-3); margin-top: 2px; }

.info { flex: 1; margin-left: 12px; min-width: 0; }
.name-row { display: flex; align-items: center; gap: 6px; }
.name { font-size: 15px; font-weight: 600; color: var(--text-1); }
.meta { font-size: 12px; color: var(--text-3); margin: 4px 0; }
.cap { font-size: 12px; color: var(--danger); font-weight: 500; }

.book {
  padding: 6px 16px;
  font-size: 13px;
  border-radius: 16px;
}
</style>
