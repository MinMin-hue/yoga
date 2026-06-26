<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { api, type Schedule } from '@/api'
import { auth } from '@/store/auth'
import { today, formatTime } from '@/utils/format'

// =================== 顶部 Hero 头图 ===================
const scenes = [
  { id: 'cover', label: '封面' },
  { id: 'env', label: '场馆环境' }
]
const activeScene = ref('cover')

// =================== 品牌信息 ===================
const brand = {
  name: '灵伽瑜伽',
  badge: '初/莲',
  desc: '让身体和心灵在这里相遇，记录每个\n不一样的你',
  phone: '400-888-8888'
}

// =================== 常用功能（8 宫格） ===================
type FeatureId =
  | 'hot' | 'class' | 'preview' | 'notice'
  | 'trial' | 'photo' | 'test' | 'checkin'

interface Feature {
  id: FeatureId
  label: string
  color: string
  bg: string
  path?: string
}

const features: Feature[] = [
  { id: 'hot',     label: '热门课程', color: '#FF6B4A', bg: '#FFEAE3', path: '/pages/schedule/list' },
  { id: 'class',   label: '班课',     color: '#FF8A3D', bg: '#FFEEDD', path: '/pages/schedule/list' },
  { id: 'preview', label: '场馆预览', color: '#4A8DFF', bg: '#E6F0FF' },
  { id: 'notice',  label: '场馆公告', color: '#23B573', bg: '#E5F6EE' },
  { id: 'trial',   label: '体验课',   color: '#A06BFF', bg: '#F0E8FF', path: '/pages/schedule/list' },
  { id: 'photo',   label: '照片墙',   color: '#FF7AAE', bg: '#FFE9F1' },
  { id: 'test',    label: '我的体测', color: '#00B8C4', bg: '#DDF5F7', path: '/pages/member/profile' },
  { id: 'checkin', label: '打卡',     color: '#F5A623', bg: '#FFF1DA' }
]

const onFeature = (f: Feature) => {
  if (f.path) return uni.navigateTo({ url: f.path })
  uni.showToast({ title: `${f.label} 即将上线`, icon: 'none' })
}

// =================== 主操作卡 ===================
const goBook = () => uni.navigateTo({ url: '/pages/schedule/list' })
const goCheckin = () => uni.showToast({ title: '签到成功', icon: 'success' })
const goCall = () => uni.makePhoneCall({ phoneNumber: brand.phone })

// =================== 金牌教练 ===================
interface Coach {
  id: number
  name: string
  title: string
  tags: string[]
  years: number
  classes: number
  avatar: string
  gradient: string
}

const coaches = ref<Coach[]>([
  {
    id: 1, name: '林清婉', title: '高级瑜伽导师', tags: ['哈他', '阴瑜伽'],
    years: 8, classes: 1260,
    avatar: 'L', gradient: 'linear-gradient(135deg, #FF9966 0%, #FF5E62 100%)'
  },
  {
    id: 2, name: '苏念', title: '普拉提主教', tags: ['普拉提', '体态'],
    years: 6, classes: 980,
    avatar: 'S', gradient: 'linear-gradient(135deg, #A18CD1 0%, #FBC2EB 100%)'
  },
  {
    id: 3, name: '夏目', title: '流瑜伽教练', tags: ['流瑜伽', '减脂'],
    years: 5, classes: 740,
    avatar: 'X', gradient: 'linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%)'
  }
])

const goCoachList = () => uni.showToast({ title: '更多教练', icon: 'none' })
const goCoachDetail = (id: number) => uni.showToast({ title: `教练 ${id}`, icon: 'none' })

// =================== 今日课程 ===================
const schedules = ref<Schedule[]>([])

const load = async () => {
  try {
    const s = await api.h5.schedules(today())
    schedules.value = (s || []).slice(0, 3)
  } catch {
    /* request 已统一提示 */
  }
}

onShow(() => {
  if (!auth.token) uni.reLaunch({ url: '/pages/login/login' })
})
onMounted(load)
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh() })
</script>

<template>
  <view class="page">
    <!-- 顶部 Hero -->
    <view class="hero">
      <view class="hero-bg">
        <view class="hero-deco hero-deco-1" />
        <view class="hero-deco hero-deco-2" />
      </view>
      <view class="hero-tabs">
        <view
          v-for="s in scenes"
          :key="s.id"
          class="hero-tab"
          :class="{ active: activeScene === s.id }"
          @click="activeScene = s.id"
        >{{ s.label }}</view>
      </view>
    </view>

    <!-- 品牌信息 -->
    <view class="brand">
      <view class="brand-left">
        <view class="brand-logo">灵</view>
        <view class="brand-info">
          <view class="brand-name">
            <text>{{ brand.name }}</text>
            <text class="brand-badge">{{ brand.badge }}</text>
          </view>
          <view class="brand-desc">{{ brand.desc }}</view>
        </view>
      </view>
      <view class="brand-phone" @click="goCall">
        <view class="phone-icon">
          <view class="phone-handle" />
          <view class="phone-body" />
        </view>
        <text>电话</text>
      </view>
    </view>

    <!-- 主操作卡 -->
    <view class="actions">
      <view class="action-card" @click="goBook">
        <view class="action-text">
          <view class="action-title">约课</view>
          <view class="action-sub">勤加练习, 身心更健康</view>
        </view>
        <view class="action-btn">去约课</view>
        <view class="action-deco action-deco-1">◐</view>
      </view>
      <view class="action-card" @click="goCheckin">
        <view class="action-text">
          <view class="action-title">签到</view>
          <view class="action-sub">坚持自律, 记录美好生活</view>
        </view>
        <view class="action-btn">去签到</view>
        <view class="action-deco action-deco-2">✓</view>
      </view>
    </view>

    <!-- 常用功能 -->
    <view class="section">
      <view class="section-title">常用功能</view>
      <view class="feature-grid">
        <view
          v-for="f in features"
          :key="f.id"
          class="feature-item"
          @click="onFeature(f)"
        >
          <view class="feature-icon" :style="{ background: f.bg, color: f.color }">
            <text class="feature-emoji">{{
              f.id === 'hot' ? '▤' :
              f.id === 'class' ? '⊞' :
              f.id === 'preview' ? '⌂' :
              f.id === 'notice' ? '◉' :
              f.id === 'trial' ? '◍' :
              f.id === 'photo' ? '▦' :
              f.id === 'test' ? '◐' : '✓'
            }}</text>
          </view>
          <text class="feature-label">{{ f.label }}</text>
        </view>
      </view>
    </view>

    <!-- 金牌教练 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">金牌教练</view>
        <view class="section-more" @click="goCoachList">
          <text>更多</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <scroll-view class="coach-scroll" scroll-x>
        <view
          v-for="c in coaches"
          :key="c.id"
          class="coach-card"
          @click="goCoachDetail(c.id)"
        >
          <view class="coach-avatar" :style="{ background: c.gradient }">{{ c.avatar }}</view>
          <view class="coach-name">{{ c.name }}</view>
          <view class="coach-title">{{ c.title }}</view>
          <view class="coach-tags">
            <text v-for="t in c.tags" :key="t" class="coach-tag">{{ t }}</text>
          </view>
          <view class="coach-meta">{{ c.years }}年教龄 · {{ c.classes }}节课</view>
        </view>
      </scroll-view>
    </view>

    <!-- 今日课程 -->
    <view class="section">
      <view class="section-title">今日课程</view>
      <view v-if="schedules.length === 0" class="empty">今日暂无课程</view>
      <view
        v-for="s in schedules"
        :key="s.id"
        class="schedule-item"
        @click="uni.navigateTo({ url: `/pages/schedule/detail?id=${s.id}` })"
      >
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

/* ===== Hero ===== */
.hero {
  position: relative;
  height: 220px;
  overflow: hidden;
}
.hero-bg {
  position: absolute; inset: 0;
  background:
    linear-gradient(180deg, rgba(0,0,0,.05) 0%, rgba(0,0,0,.45) 100%),
    linear-gradient(135deg, #b8a493 0%, #8b7a6a 45%, #5b4d40 100%);
}
.hero-deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(20px);
  opacity: .35;
}
.hero-deco-1 {
  width: 240px; height: 240px;
  top: -60px; right: -40px;
  background: radial-gradient(circle, #ffd9b8 0%, transparent 70%);
}
.hero-deco-2 {
  width: 180px; height: 180px;
  bottom: -40px; left: 20px;
  background: radial-gradient(circle, #d4b896 0%, transparent 70%);
}
.hero-tabs {
  position: absolute;
  right: 16px;
  bottom: 16px;
  display: flex;
  gap: 8px;
}
.hero-tab {
  padding: 6px 14px;
  background: rgba(0, 0, 0, .35);
  color: #fff;
  font-size: 12px;
  border-radius: 16px;
  backdrop-filter: blur(8px);
}
.hero-tab.active {
  background: rgba(255, 255, 255, .9);
  color: #333;
  font-weight: 600;
}

/* ===== 品牌信息 ===== */
.brand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  margin: -24px 12px 0;
  border-radius: 14px;
  padding: 14px 16px;
  position: relative;
  z-index: 2;
  box-shadow: 0 4px 16px rgba(0, 0, 0, .05);
}
.brand-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}
.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5e9d4 0%, #d4b896 100%);
  color: #6b4f2a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,.08);
}
.brand-info { flex: 1; min-width: 0; }
.brand-name {
  display: flex;
  align-items: center;
  font-size: 17px;
  font-weight: 700;
  color: #1f1f1f;
}
.brand-badge {
  margin-left: 8px;
  font-size: 11px;
  font-weight: 500;
  color: #6b4f2a;
  background: #f5e9d4;
  padding: 2px 8px;
  border-radius: 10px;
}
.brand-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.5;
  white-space: pre-line;
}
.brand-phone {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #6b6b6b;
  font-size: 12px;
  padding-left: 12px;
}
.phone-icon {
  position: relative;
  width: 22px;
  height: 22px;
  margin-bottom: 2px;
}
.phone-handle {
  position: absolute;
  width: 8px; height: 4px;
  background: #6b6b6b;
  border-radius: 2px;
  top: 0; left: 50%;
  transform: translateX(-50%);
}
.phone-body {
  position: absolute;
  width: 16px; height: 20px;
  background: #6b6b6b;
  border-radius: 3px;
  bottom: 0; left: 50%;
  transform: translateX(-50%);
}

/* ===== 主操作卡 ===== */
.actions {
  display: flex;
  gap: 10px;
  padding: 12px;
}
.action-card {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  border-radius: 12px;
  padding: 14px;
  color: #fff;
  overflow: hidden;
  height: 96px;
}
.action-text { position: relative; z-index: 2; }
.action-title {
  font-size: 18px;
  font-weight: 700;
}
.action-sub {
  font-size: 11px;
  opacity: .9;
  margin-top: 2px;
}
.action-btn {
  position: absolute;
  left: 14px;
  bottom: 12px;
  z-index: 2;
  background: #fff;
  color: #FF6B3D;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
}
.action-deco {
  position: absolute;
  right: 10px;
  bottom: 8px;
  font-size: 56px;
  font-weight: 700;
  color: rgba(255, 255, 255, .25);
  line-height: 1;
  z-index: 1;
}

/* ===== Section ===== */
.section {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  padding: 16px 0;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 12px;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f1f1f;
  padding: 0 16px 12px;
}
.section-header .section-title { padding: 0; }
.section-more {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #999;
}
.section-more .arrow {
  font-size: 16px;
  margin-left: 2px;
  line-height: 1;
}

/* ===== Feature Grid ===== */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 16px;
  padding: 0 8px;
}
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 6px;
}
.feature-emoji {
  font-weight: 700;
}
.feature-label {
  font-size: 12px;
  color: #4a4a4a;
}

/* ===== Coach Scroll ===== */
.coach-scroll {
  white-space: nowrap;
  padding: 0 12px 4px;
}
.coach-card {
  display: inline-block;
  width: 140px;
  margin-right: 10px;
  background: #fafafa;
  border-radius: 12px;
  padding: 12px;
  vertical-align: top;
}
.coach-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin: 0 auto 8px;
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.coach-name {
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #1f1f1f;
}
.coach-title {
  text-align: center;
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}
.coach-tags {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 6px;
  flex-wrap: wrap;
}
.coach-tag {
  font-size: 10px;
  color: #6b4f2a;
  background: #f5e9d4;
  padding: 1px 6px;
  border-radius: 8px;
}
.coach-meta {
  text-align: center;
  font-size: 10px;
  color: #b5b5b5;
  margin-top: 4px;
}

/* ===== 今日课程 ===== */
.schedule-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
}
.schedule-item:last-child { border-bottom: none; }
.time {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
  margin-right: 12px;
  min-width: 56px;
}
.info { flex: 1; min-width: 0; }
.name { font-size: 15px; font-weight: 500; }
.meta { color: #909399; font-size: 12px; margin-top: 4px; }
.book { padding: 6px 14px; font-size: 13px; }
.empty { text-align: center; color: #909399; padding: 24px; }
</style>
