<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">本月营业额</div>
          <div class="num">¥ {{ data.revenue?.total || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">本月订单数</div>
          <div class="num">{{ data.revenue?.orderCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">本月新增会员</div>
          <div class="num">{{ data.member?.newCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">本月活跃会员</div>
          <div class="num">{{ data.member?.activeCount || 0 }}</div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card>
          <template #header><b>课程上座率 TOP</b></template>
          <el-table :data="(data.course || []).slice(0, 5)" size="default">
            <el-table-column prop="courseTypeName" label="课程" />
            <el-table-column prop="scheduleCount" label="排课数" width="90" />
            <el-table-column prop="bookedCount" label="已约人数" width="100" />
            <el-table-column prop="rate" label="上座率" width="100">
              <template #default="{ row }">{{ row.rate }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><b>教练课时数</b></template>
          <el-table :data="(data.coach || []).slice(0, 5)" size="default">
            <el-table-column prop="coachName" label="教练" />
            <el-table-column prop="scheduleCount" label="排课数" width="90" />
            <el-table-column prop="studentCount" label="学员数" width="90" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { statisticsApi } from '@/api'

const data = ref<any>({})
const load = async () => {
  const [rev, mem, course, coach] = await Promise.all([
    statisticsApi.revenue('month'),
    statisticsApi.member('month'),
    statisticsApi.course('month'),
    statisticsApi.coach('month')
  ])
  data.value = { revenue: rev.data, member: mem.data, course: course.data, coach: coach.data }
}
onMounted(load)
</script>
