<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📅 排课管理</h2>
        <div class="page-subtitle">为课程安排教练、教室与时间, 会员即可预约</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增排课</el-button>
    </div>

    <div class="toolbar">
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 260px" />
      <el-select v-model="filter.courseTypeId" placeholder="课程类型" clearable style="width: 160px">
        <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-model="filter.coachId" placeholder="教练" clearable style="width: 140px">
        <el-option v-for="c in coaches" :key="c.id" :label="c.realName" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="课程" min-width="160">
          <template #default="{ row }">
            <div class="course-cell">
              <div class="course-emoji">{{ emojiFor(row.courseTypeName) }}</div>
              <span class="text-bold">{{ row.courseTypeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="教练" width="120">
          <template #default="{ row }">
            <div class="cell-user-sm">
              <div class="user-avatar-sm">{{ (row.coachName || '?').charAt(0) }}</div>
              <span>{{ row.coachName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roomName" label="教室" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="容量" width="180">
          <template #default="{ row }">
            <div class="capacity-cell">
              <div class="capacity-bar" :class="rateClass(row.bookedCount / row.capacity)">
                <span :style="{ width: Math.min(row.bookedCount / row.capacity * 100, 100) + '%' }"></span>
              </div>
              <span class="capacity-text">{{ row.bookedCount }} / {{ row.capacity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['badge', statusBadge(row.status)]">
              <span class="badge-dot"></span>
              {{ statusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="onViewBookings(row)">预约名单</el-button>
            <el-button v-if="row.status === 'SCHEDULED'" size="small" link type="warning" @click="onCancel(row)">取消</el-button>
            <el-popconfirm title="确认删除?" @confirm="onDelete(row)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination
      class="y-pagination"
      v-model:current-page="filter.pageNum" v-model:page-size="filter.pageSize"
      :total="total" @current-change="(p: number) => { filter.pageNum = p; load() }"
      layout="total, prev, pager, next, jumper"
    />

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
            <el-radio-button value="ONCE">单次</el-radio-button>
            <el-radio-button value="DAILY">每日</el-radio-button>
            <el-radio-button value="WEEKLY">每周</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="重复次数" v-if="form.repeatType !== 'ONCE'">
          <el-input-number v-model="form.repeatCount" :min="1" :max="30" />
          <span class="text-muted" style="margin-left: 8px;">最多支持 30 次</span>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit">保 存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bookingVisible" title="预约名单" width="800">
      <el-table :data="bookings" stripe>
        <el-table-column prop="memberName" label="会员" />
        <el-table-column prop="cardNo" label="卡号" width="180">
          <template #default="{ row }"><span class="mono">{{ row.cardNo }}</span></template>
        </el-table-column>
        <el-table-column prop="costTimes" label="扣次" width="80" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <span :class="['badge', bookingBadge(row.status)]">
              <span class="badge-dot"></span>
              {{ bookingName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="bookedAt" label="预约时间" width="170" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BOOKED'" size="small" type="success" @click="onCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 'CHECKED_IN'" size="small" type="primary" @click="onComplete(row)">核销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
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
const filter = reactive<any>({ startDate: '', endDate: '', courseTypeId: undefined, coachId: undefined, pageNum: 1, pageSize: 20 })
const form = reactive<any>({ courseTypeId: null, coachId: null, roomId: null, startTime: '', endTime: '', capacity: 20, repeatType: 'ONCE', repeatCount: 1, remark: '' })

const statusName = (s: string) => ({ SCHEDULED: '已排', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' } as any)[s] || s
const statusBadge = (s: string) => ({ SCHEDULED: 'info', ONGOING: 'warning', FINISHED: 'muted', CANCELLED: 'danger' } as any)[s] || 'muted'
const bookingName = (s: string) => ({ BOOKED: '已预约', CHECKED_IN: '已签到', COMPLETED: '已核销', CANCELLED: '已取消', NO_SHOW: '爽约' } as any)[s] || s
const bookingBadge = (s: string) => ({ BOOKED: 'info', CHECKED_IN: 'warning', COMPLETED: 'success', CANCELLED: 'muted', NO_SHOW: 'danger' } as any)[s] || 'muted'
const rateClass = (rate: number) => {
  if (rate >= 1) return 'full'
  if (rate >= 0.7) return 'warn'
  return ''
}
const emojiFor = (name: string) => {
  if (!name) return '🧘'
  if (name.includes('哈')) return '🧘‍♀️'
  if (name.includes('流')) return '💃'
  if (name.includes('阴')) return '🌙'
  if (name.includes('热')) return '🔥'
  if (name.includes('私')) return '👤'
  return '🧘'
}

const load = async () => {
  if (dateRange.value) { filter.startDate = dateRange.value[0]; filter.endDate = dateRange.value[1] }
  else { filter.startDate = ''; filter.endDate = '' }
  const r: any = await courseApi.schedulePage(filter)
  list.value = r.data.list
  total.value = r.data.total
}
const onReset = () => { dateRange.value = null; filter.courseTypeId = undefined; filter.coachId = undefined; load() }
const onAdd = () => { Object.assign(form, { courseTypeId: null, coachId: null, roomId: null, startTime: '', endTime: '', capacity: 20, repeatType: 'ONCE', repeatCount: 1, remark: '' }); dialogVisible.value = true }
const onSubmit = async () => {
  if (!form.courseTypeId || !form.coachId || !form.startTime || !form.endTime) { ElMessage.warning('请填写完整'); return }
  await courseApi.scheduleUpsert(form)
  ElMessage.success('已排课')
  dialogVisible.value = false
  load()
}
const onCancel = async (row: any) => { await ElMessageBox.confirm('确认取消该排课?', '提示', { type: 'warning' }); await courseApi.scheduleCancel(row.id); ElMessage.success('已取消'); load() }
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

<style scoped>
.y-pagination { margin-top: 16px; justify-content: flex-end; display: flex; }

.course-cell { display: flex; align-items: center; gap: 8px; }
.course-emoji { font-size: 20px; }
.cell-user-sm { display: flex; align-items: center; gap: 8px; }
.user-avatar-sm {
  width: 26px; height: 26px; border-radius: 50%;
  background: var(--y-gradient-cool);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600;
}
.capacity-cell { display: flex; align-items: center; gap: 6px; }
.mono { font-family: "JetBrains Mono", monospace; color: var(--y-primary); }
</style>
