<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">👥 会员管理</h2>
        <div class="page-subtitle">管理会员信息、状态</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增会员</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="🔍 姓名 / 手机号" style="width: 220px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="会员" min-width="160">
          <template #default="{ row }">
            <div class="member-cell">
              <div class="mini-avatar">{{ (row.nickname || row.phone || '?').charAt(0) }}</div>
              <div>
                <div class="member-name">{{ row.nickname || row.phone || '未命名' }}</div>
                <div class="member-phone">{{ row.phone || '未填写手机号' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <span class="label-chip">{{ { 0: '未知', 1: '男', 2: '女' }[row.gender] || '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['badge', row.status === 1 ? 'success' : 'muted']">
              <span class="badge-dot"></span>
              {{ row.status === 1 ? '正常' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
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
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" @current-change="(p: number) => load(p)"
      layout="total, prev, pager, next, jumper"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会员' : '新增会员'" width="480">
      <el-form :model="form" label-width="90px">
        <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="会员昵称" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
            <el-radio :value="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit">保 存</el-button>
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
const dialogVisible = ref(false)
const query = reactive({ keyword: '', status: null as any, pageNum: 1, pageSize: 20 })
const form = reactive<any>({ id: null, nickname: '', phone: '', gender: 0, status: 1 })

const load = async (page = 1) => {
  query.pageNum = page
  const r: any = await memberApi.page(query)
  list.value = r.data.list; total.value = r.data.total
}
const onReset = () => { query.keyword = ''; query.status = null; load(1) }
const onAdd = () => { Object.assign(form, { id: null, nickname: '', phone: '', gender: 0, status: 1 }); dialogVisible.value = true }
const onEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const onDelete = async (row: any) => { await memberApi.delete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => {
  if (!form.phone) { ElMessage.warning('请填写手机号'); return }
  if (form.id) await memberApi.update(form)
  else await memberApi.create(form)
  ElMessage.success('已保存'); dialogVisible.value = false; load()
}
onMounted(load)
</script>

<style scoped>
.y-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }

.member-cell { display: flex; align-items: center; gap: 10px; }
.mini-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--y-gradient-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600; flex-shrink: 0;
}
.member-name { font-size: 14px; font-weight: 600; color: var(--y-text); }
.member-phone { font-size: 12px; color: var(--y-text-muted); margin-top: 2px; }
</style>
