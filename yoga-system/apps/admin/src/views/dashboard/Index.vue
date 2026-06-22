<template>
  <div class="page">
    <!-- 欢迎横幅 -->
    <div class="page-hero">
      <h2>👋 你好, {{ user.profile?.realName || '管理员' }}</h2>
      <p>{{ todayText }} · 今天又是元气满满的一天</p>
      <div class="hero-actions">
        <el-button type="primary" :icon="Plus" @click="$router.push('/schedule')">安排课程</el-button>
        <el-button :icon="User" @click="$router.push('/member')">查看会员</el-button>
      </div>
    </div>

    <!-- 4 个统计卡 -->
    <div class="stat-grid">
      <div class="stat-card primary">
        <div class="stat-icon"><el-icon><Money /></el-icon></div>
        <div class="stat-num">¥ {{ stats.todayRevenue ?? 0 }}</div>
        <div class="stat-label">今日营收</div>
        <div class="stat-trend">月累计 ¥ {{ stats.monthRevenue ?? 0 }}</div>
      </div>
      <div class="stat-card warm">
        <div class="stat-icon"><el-icon><Calendar /></el-icon></div>
        <div class="stat-num">{{ stats.todayBookings ?? 0 }}</div>
        <div class="stat-label">今日预约</div>
        <div class="stat-trend">已签到 {{ stats.todayCheckIns ?? 0 }}</div>
      </div>
      <div class="stat-card cool">
        <div class="stat-icon"><el-icon><User /></el-icon></div>
        <div class="stat-num">{{ stats.activeMembers ?? 0 }}</div>
        <div class="stat-label">活跃会员</div>
        <div class="stat-trend">总会员 {{ stats.totalMembers ?? 0 }}</div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon"><el-icon><DataLine /></el-icon></div>
        <div class="stat-num">{{ stats.attendanceRate ?? 0 }}%</div>
        <div class="stat-label">平均出勤率</div>
        <div class="stat-trend">近 7 天</div>
      </div>
    </div>

    <!-- 课程上座率 / 教练课时榜 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="14">
        <div class="y-card">
          <div class="y-card-header">
            <div class="y-card-title">📊 课程上座率 Top</div>
            <el-button text type="primary" @click="$router.push('/statistics')">查看更多 →</el-button>
          </div>
          <div v-for="(c, i) in topCourses" :key="c.courseTypeId" class="rank-row">
            <div class="rank-no" :class="rankClass(i)">{{ i + 1 }}</div>
            <div class="rank-info">
              <div class="rank-name">{{ c.courseTypeName }}</div>
              <div class="capacity-bar" style="width: 100%"><span :style="{ width: c.rate + '%' }"></span></div>
            </div>
            <div class="rank-rate">{{ c.rate }}%</div>
          </div>
          <div v-if="!topCourses.length" class="empty-state"><div class="empty-icon">📊</div>暂无数据</div>
        </div>
      </el-col>
      <el-col :xs="24" :md="10">
        <div class="y-card">
          <div class="y-card-header">
            <div class="y-card-title">🏆 教练课时榜</div>
          </div>
          <div v-for="(c, i) in topCoaches" :key="c.coachId" class="rank-row">
            <div class="rank-no" :class="rankClass(i)">{{ i + 1 }}</div>
            <div class="rank-info">
              <div class="coach-mini">
                <div class="mini-avatar">{{ (c.coachName || '?').charAt(0) }}</div>
                <span class="rank-name">{{ c.coachName }}</span>
              </div>
            </div>
            <div class="rank-rate">{{ c.hours }} 课时</div>
          </div>
          <div v-if="!topCoaches.length" class="empty-state"><div class="empty-icon">🏆</div>暂无数据</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, User, Money, Calendar, DataLine } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { statisticsApi } from '@/api'

const user = useUserStore()
const stats = reactive<any>({})
const topCourses = ref<any[]>([])
const topCoaches = ref<any[]>([])

const todayText = computed(() => {
  const d = new Date()
  const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${w}`
})
const rankClass = (i: number) => i === 0 ? 'gold' : i === 1 ? 'silver' : i === 2 ? 'bronze' : ''

onMounted(async () => {
  try {
    const r: any = await statisticsApi.overview()
    Object.assign(stats, r.data)
    topCourses.value = r.data.topCourses || []
    topCoaches.value = r.data.topCoaches || []
  } catch (e) { console.warn('dashboard load fail', e) }
})
</script>

<style scoped>
.stat-grid { display: grid; gap: 16px; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
.rank-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px dashed var(--y-border); }
.rank-row:last-child { border-bottom: none; }
.rank-no {
  width: 28px; height: 28px; border-radius: 50%;
  background: #e2e8f0; color: var(--y-text-secondary);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; flex-shrink: 0;
}
.rank-no.gold { background: linear-gradient(135deg, #fbbf24, #f59e0b); color: #fff; }
.rank-no.silver { background: linear-gradient(135deg, #cbd5e1, #94a3b8); color: #fff; }
.rank-no.bronze { background: linear-gradient(135deg, #fb923c, #c2410c); color: #fff; }
.rank-info { flex: 1; min-width: 0; }
.rank-name { font-size: 13px; color: var(--y-text); margin-bottom: 4px; }
.rank-rate { font-size: 14px; font-weight: 600; color: var(--y-primary); }
.coach-mini { display: flex; align-items: center; gap: 8px; }
.mini-avatar {
  width: 26px; height: 26px; border-radius: 50%;
  background: var(--y-gradient-warm); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600;
}
</style>
