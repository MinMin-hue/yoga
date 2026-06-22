<template>
  <div class="page">
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="教室名" />
      <el-table-column prop="capacity" label="容量" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="onDelete(row)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-button type="primary" @click="onAdd" style="margin-top: 12px">新增教室</el-button>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑教室' : '新增教室'" width="400">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { courseApi } from '@/api'

const list = ref<any[]>([])
const dialogVisible = ref(false)
const form = reactive<any>({ id: null, name: '', capacity: 20, status: 1 })

const load = async () => { const r: any = await courseApi.roomList(); list.value = r.data }
const onAdd = () => { Object.assign(form, { id: null, name: '', capacity: 20, status: 1 }); dialogVisible.value = true }
const onEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const onDelete = async (row: any) => { await courseApi.roomDelete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => { await courseApi.roomUpsert(form); ElMessage.success('已保存'); dialogVisible.value = false; load() }
onMounted(load)
</script>
