<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📅 排课管理</h2>
        <div class="page-subtitle">配置每日课程安排</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增排课</el-button>
    </div>

    <div class="toolbar">
      <el-date-picker v-model="query.range" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-select v-model="query.coachId" placeholder="教练" clearable style="width: 130px">
        <el-option v-for="c in coaches" :key="c.id" :label="c.realName || c.username" :value="c.id" />
      </el-select>
      <el-select v-model="query.courseTypeId" placeholder="课程类型" clearable style="width: 140px">
        <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" stripe>
        <el-table-column label="课程" min-width="180">
          <template #default="{ row }">
            <div class="course-cell">
              <span class="course-emoji">{{ catEmoji(row.courseCategory) }}</span>
              <div>
                <div class="course-name">{{ row.courseTypeName || '-' }}</div>
                <div class="course-meta">{{ row.roomName || '-' }} · {{ row.durationMinutes || 60 }}分钟</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="教练" width="140">
          <template #default="{ row }">
            <div class="coach-mini">
              <div class="mini-avatar">{{ (row.coachName || '?').charAt(0) }}</div>
              <span>{{ row.coachName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column label="容量" width="170" align="center">
          <template #default="{ row }">
            <div :class="['capacity-bar', row.bookedCount / row.capacity >= 1 ? 'full' : row.bookedCount / row.capacity >= 0.8 ? 'warn' : '']">
              <span :style="{ width: (Math.min(row.bookedCount / row.capacity, 1) * 100) + '%' }"></span>
            </div>
            <span class="capacity-text">{{ row.bookedCount }}/{{ row.capacity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <span :class="['badge', statusBadge(row.status)]">
              <span class="badge-dot"></span>
              {{ statusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="onViewBooking(row)">预约名单</el-button>
            <el-button size="small" link type="primary" @click="onEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该排课?" @confirm="onDelete(row)">
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
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" @current-change="(p: number) => load(p)"
      layout="total, prev, pager, next, jumper"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑排课' : '新增排课'" width="520">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程类型">
          <el-select v-model="form.courseTypeId" style="width: 100%">
            <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练"><el-select v-model="form.coachId" style="width: 100%"><el-option v-for="c in coaches" :key="c.id" :label="c.realName || c.username" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="教室"><el-select v-model="form.roomId" style="width: 100%"><el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" style="width: 100%" /></el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="form.durationMinutes" :min="15" :step="15" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit">保 存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bookingVisible" title="📝 预约名单" width="780">
      <el-table :data="bookingList">
        <el-table-column prop="bookingNo" label="预约号" width="190">
          <template #default="{ row }"><span class="mono">{{ row.bookingNo }}</span></template>
        </el-table-column>
        <el-table-column prop="memberName" label="会员" width="100" />
        <el-table-column prop="memberPhone" label="手机号" width="130" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['badge', row.status === 'BOOKED' ? 'info' : row.status === 'CHECKED_IN' ? 'warning' : 'success']">
              <span class="badge-dot"></span>
              {{ { BOOKED: '已预约', CHECKED_IN: '已签到', COMPLETED: '已核销' }[row.status] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="bookedAt" label="预约时间" />
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BOOKED'" size="small" type="success" link @click="onCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 'BOOKED'" size="small" type="danger" link @click="onCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { courseApi, bookingApi } from '@/api'

const list = ref<any[]>([])
const total = ref(0)
const coaches = ref<any[]>([])
const rooms = ref<any[]>([])
const courseTypes = ref<any[]>([])
const dialogVisible = ref(false)
const bookingVisible = ref(false)
const bookingList = ref<any[]>([])
const currentSchedule = ref<any>(null)
const query = reactive({ range: null as any, coachId: null as any, courseTypeId: null as any, pageNum: 1, pageSize: 20 })
const form = reactive<any>({ id: null, courseTypeId: null, coachId: null, roomId: null, startTime: null, durationMinutes: 60, capacity: 12 })

const statusName = (s: string) => ({ OPEN: '可预约', FULL: '已满', CANCELLED: '已取消', DONE: '已结束' } as any)[s] || s
const statusBadge = (s: string) => ({ OPEN: 'success', FULL: 'warning', CANCELLED: 'muted', DONE: 'muted' } as any)[s] || 'muted'
const catEmoji = (c?: string) => ({ GROUP: '🧘‍♀️', SPECIAL: '✨', PRIVATE: '👤' } as any)[c || ''] || '🧘'

const load = async (page = 1) => {
  query.pageNum = page
  const r: any = await courseApi.schedulePage(query)
  list.value = r.data.list; total.value = r.data.total
}
const loadOpts = async () => {
  coaches.value = (await courseApi.coachList()).data
  rooms.value = (await courseApi.roomList()).data
  courseTypes.value = (await courseApi.typeList()).data
}
const onReset = () => { query.range = null; query.coachId = null; query.courseTypeId = null; load(1) }
const onAdd = () => { Object.assign(form, { id: null, courseTypeId: courseTypes.value[0]?.id, coachId: coaches.value[0]?.id, roomId: rooms.value[0]?.id, startTime: new Date(), durationMinutes: 60, capacity: 12 }); dialogVisible.value = true }
const onEdit = (row: any) => { Object.assign(form, row, { startTime: new Date(row.startTime) }); dialogVisible.value = true }
const onDelete = async (row: any) => { await courseApi.scheduleDelete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => {
  if (form.id) await courseApi.scheduleUpdate(form)
  else await courseApi.scheduleCreate(form)
  ElMessage.success('已保存'); dialogVisible.value = false; load()
}
const onViewBooking = async (row: any) => {
  currentSchedule.value = row
  const r: any = await bookingApi.bySchedule(row.id)
  bookingList.value = r.data
  bookingVisible.value = true
}
const onCheckIn = async (row: any) => { await bookingApi.checkIn(row.id); ElMessage.success('已签到'); onViewBooking(currentSchedule.value) }
const onCancel = async (row: any) => { await bookingApi.cancel(row.id); ElMessage.success('已取消'); onViewBooking(currentSchedule.value) }
onMounted(() => { load(); loadOpts() })
</script>

<style scoped>
.y-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.mono { font-family: "JetBrains Mono", monospace; color: var(--y-primary); font-size: 12px; }

.course-cell { display: flex; align-items: center; gap: 10px; }
.course-emoji { font-size: 22px; }
.course-name { font-size: 14px; font-weight: 600; color: var(--y-text); }
.course-meta { font-size: 12px; color: var(--y-text-muted); margin-top: 2px; }

.coach-mini { display: flex; align-items: center; gap: 8px; }
.mini-avatar {
  width: 26px; height: 26px; border-radius: 50%;
  background: var(--y-gradient-warm); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600;
}
</style>
