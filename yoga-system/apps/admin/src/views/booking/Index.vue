<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📋 预约管理</h2>
        <div class="page-subtitle">查看会员预约, 处理签到与核销</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="🔍 会员名 / 预约号" style="width: 220px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="已预约" value="BOOKED" />
        <el-option label="已签到" value="CHECKED_IN" />
        <el-option label="已核销" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="爽约" value="NO_SHOW" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" stripe>
        <el-table-column prop="bookingNo" label="预约号" width="200">
          <template #default="{ row }"><span class="mono">{{ row.bookingNo }}</span></template>
        </el-table-column>
        <el-table-column prop="memberName" label="会员" width="120" />
        <el-table-column prop="scheduleId" label="课程ID" width="80" align="center" />
        <el-table-column prop="costTimes" label="扣次" width="80" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <span :class="['badge', statusBadge(row.status)]">
              <span class="badge-dot"></span>
              {{ statusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="违约" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.isPenalty === 1" class="badge danger"><span class="badge-dot"></span>违约</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="bookedAt" label="预约时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BOOKED'" size="small" type="success" @click="onCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 'CHECKED_IN'" size="small" type="primary" @click="onComplete(row)">核销</el-button>
            <span v-if="!['BOOKED','CHECKED_IN'].includes(row.status)" class="text-muted" style="font-size: 12px;">已结束</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination
      class="y-pagination"
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" @current-change="(p: number) => load(p)"
      layout="total, prev, pager, next, jumper"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { bookingApi } from '@/api'

const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', pageNum: 1, pageSize: 20 })

const statusName = (s: string) => ({ BOOKED: '已预约', CHECKED_IN: '已签到', COMPLETED: '已核销', CANCELLED: '已取消', NO_SHOW: '爽约' } as any)[s] || s
const statusBadge = (s: string) => ({ BOOKED: 'info', CHECKED_IN: 'warning', COMPLETED: 'success', CANCELLED: 'muted', NO_SHOW: 'danger' } as any)[s] || 'muted'

const load = async (page = 1) => {
  query.pageNum = page
  const r: any = await bookingApi.page(query)
  list.value = r.data.list
  total.value = r.data.total
}
const onReset = () => { query.keyword = ''; query.status = ''; load(1) }
const onCheckIn = async (row: any) => { await bookingApi.checkIn(row.id); ElMessage.success('已签到'); load() }
const onComplete = async (row: any) => { await bookingApi.complete(row.id); ElMessage.success('已核销'); load() }
onMounted(load)
</script>

<style scoped>
.y-pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
.mono { font-family: "JetBrains Mono", monospace; color: var(--y-primary); font-size: 12px; }
</style>
