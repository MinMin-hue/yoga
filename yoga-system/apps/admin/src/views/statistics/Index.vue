<template>
  <div class="page">
    <div class="toolbar">
      <el-radio-group v-model="range" @change="load">
        <el-radio-button value="day">今日</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
        <el-radio-button value="year">本年</el-radio-button>
      </el-radio-group>
    </div>
    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">营业额</div>
          <div class="num">¥ {{ data.revenue?.total || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">订单数</div>
          <div class="num">{{ data.revenue?.orderCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">退款额</div>
          <div class="num">¥ {{ data.revenue?.refundAmount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">会员总数</div>
          <div class="num">{{ data.member?.totalCount || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header><b>课程上座率</b></template>
          <el-table :data="data.course || []" size="default">
            <el-table-column prop="courseTypeName" label="课程" />
            <el-table-column prop="scheduleCount" label="排课" width="80" />
            <el-table-column prop="bookedCount" label="已约" width="80" />
            <el-table-column prop="rate" label="上座率" width="100">
              <template #default="{ row }">{{ row.rate }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><b>教练课时数</b></template>
          <el-table :data="data.coach || []" size="default">
            <el-table-column prop="coachName" label="教练" />
            <el-table-column prop="scheduleCount" label="排课" width="80" />
            <el-table-column prop="studentCount" label="学员" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header><b>订单类型分布</b></template>
      <div ref="chartRef" style="height: 320px"></div>
    </el-card>
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
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: Object.keys(m).map(k => ({ name: { PURCHASE_CARD: '购卡', RECHARGE: '充值', SINGLE_COURSE: '单课' }[k] || k, value: m[k] }))
    }]
  })
}
onMounted(load)
</script>
