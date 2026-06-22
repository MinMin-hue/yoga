<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="会员名/预约号" style="width: 200px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="已预约" value="BOOKED" />
        <el-option label="已签到" value="CHECKED_IN" />
        <el-option label="已核销" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="爽约" value="NO_SHOW" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="bookingNo" label="预约号" width="180" />
      <el-table-column prop="memberName" label="会员" width="120" />
      <el-table-column prop="scheduleId" label="课程ID" width="80" />
      <el-table-column prop="costTimes" label="扣次" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="bookedAt" label="预约时间" width="170" />
      <el-table-column label="违约" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isPenalty === 1" type="danger">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'BOOKED'" size="small" type="success" @click="onCheckIn(row)">签到</el-button>
          <el-button v-if="row.status === 'CHECKED_IN'" size="small" @click="onComplete(row)">核销</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @current-change="(p: number) => load(p)" layout="total, prev, pager, next" />
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
const statusType = (s: string) => ({ BOOKED: '', CHECKED_IN: 'warning', COMPLETED: 'success', CANCELLED: 'info', NO_SHOW: 'danger' } as any)[s] || ''

const load = async (page = 1) => {
  query.pageNum = page
  const r: any = await bookingApi.page(query)
  list.value = r.data.list
  total.value = r.data.total
}
const onCheckIn = async (row: any) => { await bookingApi.checkIn(row.id); ElMessage.success('已签到'); load() }
const onComplete = async (row: any) => { await bookingApi.complete(row.id); ElMessage.success('已核销'); load() }
onMounted(load)
</script>
