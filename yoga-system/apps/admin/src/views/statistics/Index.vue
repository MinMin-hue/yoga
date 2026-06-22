<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📊 数据统计</h2>
        <div class="page-subtitle">查看营收、会员、课程的详细数据</div>
      </div>
      <el-radio-group v-model="range" @change="load" size="default">
        <el-radio-button value="day">今日</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
        <el-radio-button value="year">本年</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card primary">
          <div class="stat-icon">💰</div>
          <div class="stat-num">¥ {{ formatNum(data.revenue?.total) }}</div>
          <div class="stat-label">营业额</div>
          <div class="stat-trend">订单 {{ data.revenue?.orderCount || 0 }} 笔</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card success">
          <div class="stat-icon">📈</div>
          <div class="stat-num">¥ {{ formatNum(data.revenue?.refundAmount) }}</div>
          <div class="stat-label">退款额</div>
          <div class="stat-trend">较上期 +0%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card warm">
          <div class="stat-icon">👥</div>
          <div class="stat-num">{{ data.member?.totalCount || 0 }}</div>
          <div class="stat-label">会员总数</div>
          <div class="stat-trend">新增 {{ data.member?.newCount || 0 }} / 活跃 {{ data.member?.activeCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card cool">
          <div class="stat-icon">📅</div>
          <div class="stat-num">{{ data.course?.reduce((s: number, c: any) => s + c.scheduleCount, 0) || 0 }}</div>
          <div class="stat-label">排课总数</div>
          <div class="stat-trend">已约 {{ data.course?.reduce((s: number, c: any) => s + c.bookedCount, 0) || 0 }} 人次</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row-gap">
      <el-col :span="12">
        <div class="y-card">
          <div class="y-card-header">
            <span class="y-card-title">📚 课程上座率</span>
            <span class="text-muted" style="font-size: 12px;">按课程统计</span>
          </div>
          <el-table :data="data.course || []" stripe>
            <el-table-column prop="courseTypeName" label="课程" />
            <el-table-column prop="scheduleCount" label="排课" width="80" align="center" />
            <el-table-column prop="bookedCount" label="已约" width="80" align="center" />
            <el-table-column label="上座率" min-width="160">
              <template #default="{ row }">
                <div class="capacity-cell">
                  <div class="capacity-bar" :class="rateClass(row.rate)">
                    <span :style="{ width: Math.min(row.rate, 100) + '%' }"></span>
                  </div>
                  <span class="capacity-text">{{ row.rate }}%</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="y-card">
          <div class="y-card-header">
            <span class="y-card-title">🧘 教练课时榜</span>
            <span class="text-muted" style="font-size: 12px;">按教练统计</span>
          </div>
          <el-table :data="data.coach || []" stripe>
            <el-table-column label="教练" min-width="120">
              <template #default="{ row }">
                <div class="cell-user-sm">
                  <div class="user-avatar-sm">{{ (row.coachName || '?').charAt(0) }}</div>
                  <span>{{ row.coachName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="scheduleCount" label="排课" width="80" align="center" />
            <el-table-column prop="studentCount" label="学员" width="80" align="center" />
            <el-table-column label="课时" width="100" align="right">
              <template #default="{ row }">
                <span class="text-bold" style="color: var(--y-primary);">{{ row.scheduleCount }} 节</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <div class="y-card row-gap">
      <div class="y-card-header">
        <span class="y-card-title">🥧 订单类型分布</span>
        <span class="text-muted" style="font-size: 12px;">饼图</span>
      </div>
      <div ref="chartRef" class="chart-area"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'

const range = ref('month')
const data = ref<any>({})
const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

const formatNum = (n: number) => Number(n || 0).toLocaleString('zh-CN')

const rateClass = (rate: number) => {
  if (rate >= 90) return 'full'
  if (rate >= 70) return 'warn'
  return ''
}

const load = async () => {
  const [rev, mem, course, coach] = await Promise.all([
    statisticsApi.revenue(range.value),
    statisticsApi.member(range.value),
    statisticsApi.course(range.value),
    statisticsApi.coach(range.value)
  ])
  data.value = { revenue: rev.data, member: mem.data, course: course.data, coach: coach.data }
  await nextTick()
  drawChart()
}

const drawChart = () => {
  if (!chartRef.value) return
  chart = chart || echarts.init(chartRef.value)
  const m = data.value.revenue?.byTypeCount || {}
  const colors = ['#667eea', '#f5576c', '#43e97b', '#fee140', '#4facfe']
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: Object.keys(m).map(k => ({ name: { PURCHASE_CARD: '购卡', RECHARGE: '充值', SINGLE_COURSE: '单课' }[k] || k, value: m[k] }))
    }]
  })
}
onMounted(load)
</script>

<style scoped>
.row-gap { margin-top: 16px; }
.chart-area { height: 340px; }

.capacity-cell { display: flex; align-items: center; gap: 8px; }
.cell-user-sm { display: flex; align-items: center; gap: 8px; }
.user-avatar-sm {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--y-gradient-cool);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600;
}
</style>
