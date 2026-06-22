<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">👥 会员管理</h2>
        <div class="page-subtitle">管理所有注册会员信息和状态</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增会员</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="🔍 手机号 / 昵称" style="width: 220px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="会员" min-width="180">
          <template #default="{ row }">
            <div class="cell-user">
              <div class="user-avatar">{{ (row.nickname || row.phone || '?').charAt(0) }}</div>
              <div>
                <div class="user-name">{{ row.nickname || '未设置' }}</div>
                <div class="user-phone">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">
            <span class="label-chip">{{ ['', '男', '女'][row.gender || 0] }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" width="120" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['badge', row.status === 1 ? 'success' : 'muted']">
              <span class="badge-dot"></span>
              {{ row.status === 1 ? '正常' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="onEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该会员?" @confirm="onDelete(row)">
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
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      @current-change="(p: number) => load(p)"
      layout="total, prev, pager, next, jumper"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会员' : '新增会员'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="选填" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日"><el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit" :loading="submitting">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { memberApi } from '@/api'

const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', status: undefined as number | undefined, pageNum: 1, pageSize: 20 })
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<any>({ id: null, phone: '', nickname: '', gender: 0, status: 1, remark: '' })
const rules = { phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }] }

const load = async (page = 1) => {
  query.pageNum = page
  loading.value = true
  const resp: any = await memberApi.page(query)
  list.value = resp.data.list
  total.value = resp.data.total
  loading.value = false
}
const onReset = () => { query.keyword = ''; query.status = undefined; load(1) }
const onAdd = () => {
  Object.assign(form, { id: null, phone: '', nickname: '', gender: 0, status: 1, remark: '', birthday: null })
  dialogVisible.value = true
}
const onEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const onDelete = async (row: any) => { await memberApi.delete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await memberApi.update(form)
    else await memberApi.create(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally { submitting.value = false }
}
onMounted(load)
</script>

<style scoped>
.y-pagination { margin-top: 16px; justify-content: flex-end; display: flex; }

.cell-user { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--y-gradient-warm);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600; flex-shrink: 0;
}
.user-name { font-size: 13px; font-weight: 600; color: var(--y-text); }
.user-phone { font-size: 12px; color: var(--y-text-muted); margin-top: 2px; }
</style>
