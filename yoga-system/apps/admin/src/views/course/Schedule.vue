<template>
  <div class="page">
    <div class="toolbar">
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 260px" />
      <el-select v-model="filter.courseTypeId" placeholder="课程类型" clearable style="width: 160px">
        <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-model="filter.coachId" placeholder="教练" clearable style="width: 140px">
        <el-option v-for="c in coaches" :key="c.id" :label="c.realName" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="onAdd">新增排课</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="courseTypeName" label="课程" width="120" />
      <el-table-column prop="coachName" label="教练" width="100" />
      <el-table-column prop="roomName" label="教室" width="100" />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="结束时间" width="160" />
      <el-table-column label="已约/容量" width="100">
        <template #default="{ row }">{{ row.bookedCount }} / {{ row.capacity }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="onViewBookings(row)">预约名单</el-button>
          <el-button size="small" type="warning" v-if="row.status === 'SCHEDULED'" @click="onCancel(row)">取消</el-button>
          <el-popconfirm title="确认删除?" @confirm="onDelete(row)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" v-model:current-page="filter.pageNum" v-model:page-size="filter.pageSize" :total="total" @current-change="(p: number) => { filter.pageNum = p; load() }" layout="total, prev, pager, next" />

    <el-dialog v-model="dialogVisible" title="新增排课" width="600">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程类型"><el-select v-model="form.courseTypeId" style="width: 100%"><el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" /></el-select></el-form-item>
        <el-form-item label="教练"><el-select v-model="form.coachId" style="width: 100%"><el-option v-for="c in coaches" :key="c.id" :label="c.realName" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="教室"><el-select v-model="form.roomId" clearable style="width: 100%"><el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="重复类型">
          <el-radio-group v-model="form.repeatType">
            <el-radio value="ONCE">单次</el-radio>
            <el-radio value="DAILY">每日</el-radio>
            <el-radio value="WEEKLY">每周</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="重复次数" v-if="form.repeatType !== 'ONCE'">
          <el-input-number v-model="form.repeatCount" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bookingVisible" title="预约名单" width="700">
      <el-table :data="bookings" border>
        <el-table-column prop="memberName" label="会员" />
        <el-table-column prop="cardNo" label="卡号" width="180" />
        <el-table-column prop="costTimes" label="扣次" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag>{{ statusName(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="bookedAt" label="预约时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BOOKED'" size="small" type="success" @click="onCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 'CHECKED_IN'" size="small" @click="onComplete(row)">核销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courseApi, bookingApi } from '@/api'

const list = ref<any[]>([])
const total = ref(0)
const courseTypes = ref<any[]>([])
const coaches = ref<any[]>([])
const rooms = ref<any[]>([])
const dialogVisible = ref(false)
const bookingVisible = ref(false)
const bookings = ref<any[]>([])
const dateRange = ref<[string, string] | null>(null)
const filter = reactive<any>({ startDate: '', endDate: '', courseTypeId: undefined, coachId: undefined, pageNum: 1, pageSize: 50 })
const form = reactive<any>({ courseTypeId: null, coachId: null, roomId: null, startTime: '', endTime: '', capacity: 20, repeatType: 'ONCE', repeatCount: 1, remark: '' })

const statusName = (s: string) => ({ SCHEDULED: '已排', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消', BOOKED: '已预约', CHECKED_IN: '已签到', COMPLETED: '已核销', NO_SHOW: '爽约', CANCELLED2: '已取消' } as any)[s] || s
const statusType = (s: string) => ({ SCHEDULED: 'success', ONGOING: 'warning', FINISHED: 'info', CANCELLED: 'danger' } as any)[s] || ''

const load = async () => {
  if (dateRange.value) { filter.startDate = dateRange.value[0]; filter.endDate = dateRange.value[1] }
  else { filter.startDate = ''; filter.endDate = '' }
  const r: any = await courseApi.schedulePage(filter)
  list.value = r.data.list
  total.value = r.data.total
}
const onAdd = () => { Object.assign(form, { courseTypeId: null, coachId: null, roomId: null, startTime: '', endTime: '', capacity: 20, repeatType: 'ONCE', repeatCount: 1, remark: '' }); dialogVisible.value = true }
const onSubmit = async () => {
  if (!form.courseTypeId || !form.coachId || !form.startTime || !form.endTime) { ElMessage.warning('请填写完整'); return }
  await courseApi.scheduleUpsert(form)
  ElMessage.success('已排课')
  dialogVisible.value = false
  load()
}
const onCancel = async (row: any) => { await ElMessageBox.confirm('确认取消?', '提示'); await courseApi.scheduleCancel(row.id); ElMessage.success('已取消'); load() }
const onDelete = async (row: any) => { await courseApi.scheduleDelete(row.id); ElMessage.success('已删除'); load() }
const onViewBookings = async (row: any) => {
  const r: any = await bookingApi.bySchedule(row.id)
  bookings.value = r.data.list
  bookingVisible.value = true
}
const onCheckIn = async (row: any) => { await bookingApi.checkIn(row.id); ElMessage.success('已签到'); onViewBookings({ id: row.scheduleId }) }
const onComplete = async (row: any) => { await bookingApi.complete(row.id); ElMessage.success('已核销'); onViewBookings({ id: row.scheduleId }) }

onMounted(async () => {
  const [ct, co, rm] = await Promise.all([courseApi.typeList(1), courseApi.coachList(), courseApi.roomList()])
  courseTypes.value = (ct as any).data
  coaches.value = (co as any).data
  rooms.value = (rm as any).data
  load()
})
</script>
