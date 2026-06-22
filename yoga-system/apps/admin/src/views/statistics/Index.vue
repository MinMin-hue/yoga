<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📊 数据统计</h2>
        <div class="page-subtitle">营收、课程、教练、会员多维度数据</div>
      </div>
    </div>

    <div class="toolbar">
      <el-radio-group v-model="range" @change="onRangeChange">
        <el-radio-button value="7">近 7 天</el-radio-button>
        <el-radio-button value="30">近 30 天</el-radio-button>
        <el-radio-button value="90">近 90 天</el-radio-button>
      </el-radio-group>
    </div>

    <div class="stat-grid">
      <div class="stat-card primary">
        <div class="stat-icon"><el-icon><Money /></el-icon></div>
        <div class="stat-num">¥ {{ stats.revenue ?? 0 }}</div>
        <div class="stat-label">总营收</div>
        <div class="stat-trend">较上周期 {{ stats.revenueDelta ?? 0 }}%</div>
      </div>
      <div class="stat-card warm">
        <div class="stat-icon"><el-icon><Calendar /></el-icon></div>
        <div class="stat-num">{{ stats.bookings ?? 0 }}</div>
        <div class="stat-label">总预约</div>
        <div class="stat-trend">签到率 {{ stats.checkInRate ?? 0 }}%</div>
      </div>
      <div class="stat-card cool">
        <div class="stat-icon"><el-icon><User /></el-icon></div>
        <div class="stat-num">{{ stats.newMembers ?? 0 }}</div>
        <div class="stat-label">新增会员</div>
        <div class="stat-trend">活跃 {{ stats.activeMembers ?? 0 }} 人</div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon"><el-icon><DataLine /></el-icon></div>
        <div class="stat-num">{{ stats.attendanceRate ?? 0 }}%</div>
        <div class="stat-label">平均出勤率</div>
        <div class="stat-trend">课程数 {{ stats.totalCourses ?? 0 }}</div>
      </div>
    </div>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="14">
        <div class="y-card">
          <div class="y-card-header">
            <div class="y-card-title">📈 课程上座率 Top 10</div>
          </div>
          <div v-for="(c, i) in topCourses" :key="i" class="rank-row">
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
          <div v-for="(c, i) in topCoaches" :key="i" class="rank-row">
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

    <div class="y-card" style="margin-top: 16px">
      <div class="y-card-header">
        <div class="y-card-title">💰 订单类型分布</div>
      </div>
      <div ref="pieRef" style="height: 320px"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Money, Calendar, User, DataLine } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'

const range = ref('7')
const stats = reactive<any>({})
const topCourses = ref<any[]>([])
const topCoaches = ref<any[]>([])
const pieRef = ref()
let pieChart: any = null

const rankClass = (i: number) => i === 0 ? 'gold' : i === 1 ? 'silver' : i === 2 ? 'bronze' : ''

const renderPie = (data: any[]) => {
  if (!pieRef.value) return
  if (!pieChart) pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    color: ['#6366f1', '#ec4899', '#4facfe', '#43e97b', '#fee140'],
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data
    }]
  })
}

const load = async () => {
  const r: any = await statisticsApi.range({ days: Number(range.value) })
  Object.assign(stats, r.data)
  topCourses.value = r.data.topCourses || []
  topCoaches.value = r.data.topCoaches || []
  await nextTick(); renderPie(r.data.orderTypeDist || [])
}
const onRangeChange = () => load()
onMounted(load)
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
