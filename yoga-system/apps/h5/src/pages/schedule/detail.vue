<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type Schedule, type MemberCard } from '@/api'
import { auth } from '@/store/auth'
import { formatTime, formatDate, today } from '@/utils/format'

const schedule = ref<Schedule | null>(null)
const cards = ref<MemberCard[]>([])
const selCard = ref<number | null>(null)
const showSheet = ref(false)
const submitting = ref(false)
const loading = ref(true)

const load = async () => {
  const pages = uni.getCurrentPages() as any[]
  const id = Number(pages[pages.length - 1]?.options?.id)
  if (!id) {
    loading.value = false
    return
  }
  try {
    const [all, c] = await Promise.all([
      api.h5.schedules(today()),
      api.h5.cards()
    ])
    schedule.value = (all || []).find((x) => x.id === id) ?? null
    cards.value = (c.cards || []).filter((x) => x.status === 'ACTIVE')
  } catch { /* */ }
  finally { loading.value = false }
}
onMounted(load)

const onBook = () => {
  if (!auth.token) return uni.navigateTo({ url: '/pages/login/login' })
  if (cards.value.length === 0) {
    return uni.showToast({ title: '您还没有可用的会员卡, 请先购卡', icon: 'none' })
  }
  showSheet.value = true
}
const doBook = async () => {
  if (!schedule.value || !selCard.value) {
    return uni.showToast({ title: '请选择会员卡', icon: 'none' })
  }
  submitting.value = true
  try {
    await api.h5.createBooking(schedule.value.id, selCard.value)
    uni.showToast({ title: '预约成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 600)
  } catch { /* */ } finally {
    submitting.value = false
    showSheet.value = false
  }
}
</script>

<template>
  <view v-if="loading" class="empty">加载中...</view>
  <view v-else-if="!schedule" class="empty">
    <view class="empty-icon">▦</view>
    <text>课程不存在或已下架</text>
  </view>
  <view v-else class="page">
    <view class="hero">
      <view class="hero-tag">课程详情</view>
      <view class="name">{{ schedule.courseTypeName }}</view>
      <view class="time">▤ {{ formatDate(schedule.startTime) }} - {{ formatTime(schedule.endTime) }}</view>
      <view class="meta">教练 {{ schedule.coachName }} · {{ schedule.roomName || '教室待定' }}</view>
      <view class="cap">已约 {{ schedule.bookedCount }} / {{ schedule.capacity }} 人</view>
    </view>

    <view class="section">
      <view class="section-title">课程说明</view>
      <view class="row"><text>单次消耗</text><text class="val">1 次数</text></view>
      <view class="row"><text>签到时间</text><text class="val">课程开始前 {{ schedule.checkinBefore }} 分钟内</text></view>
      <view class="row"><text>取消政策</text><text class="val">课程开始前可取消</text></view>
    </view>

    <view class="footer">
      <button class="btn-primary btn-block" @click="onBook">立即预约</button>
    </view>

    <view v-if="showSheet" class="mask" @click="showSheet = false">
      <view class="sheet" @click.stop>
        <view class="sheet-title">选择会员卡</view>
        <view v-if="cards.length === 0" class="empty" style="padding: 24px;">暂无可用会员卡</view>
        <view
          v-for="c in cards"
          :key="c.id"
          :class="['card-opt', selCard === c.id && 'active']"
          @click="selCard = c.id"
        >
          <view class="opt-top">
            <text class="opt-name">{{ c.cardTypeName }}</text>
            <text class="opt-no">{{ c.cardNo }}</text>
          </view>
          <view class="opt-meta">剩余 {{ c.remainTimes ?? '不限' }} 次 · 到期 {{ c.validTo?.substring(0, 10) || '永久' }}</view>
        </view>
        <button class="btn-primary btn-block" :loading="submitting" :disabled="submitting || !selCard" @click="doBook">确认预约</button>
      </view>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.hero {
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  color: #fff;
  padding: 28px 20px;
}
.hero-tag {
  display: inline-block;
  font-size: 11px;
  background: rgba(255, 255, 255, .25);
  padding: 2px 10px;
  border-radius: 12px;
  margin-bottom: 12px;
}
.name { font-size: 24px; font-weight: 700; }
.time, .meta, .cap { margin-top: 8px; opacity: .95; font-size: 13px; }
.cap { font-weight: 500; }

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  color: var(--text-2);
}
.row:last-child { border-bottom: none; }
.val { color: var(--text-1); font-weight: 500; }

.footer {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, .06);
  z-index: 10;
}

.mask {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, .5);
  z-index: 99;
  display: flex; align-items: flex-end;
}
.sheet {
  width: 100%;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 16px;
  max-height: 70vh;
  overflow-y: auto;
}
.sheet-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  text-align: center;
}
.card-opt {
  padding: 12px;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  margin-bottom: 8px;
}
.card-opt.active {
  border-color: var(--brand);
  background: var(--brand-bg);
}
.opt-top { display: flex; justify-content: space-between; align-items: center; }
.opt-name { font-size: 14px; font-weight: 600; }
.opt-no { font-size: 11px; color: var(--text-3); }
.opt-meta { color: var(--text-3); font-size: 12px; margin-top: 4px; }
</style>
