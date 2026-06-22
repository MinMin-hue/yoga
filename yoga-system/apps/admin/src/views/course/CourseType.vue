<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="onAdd">新增课程类型</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="课程名" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ { GROUP: '团课', SPECIAL: '特色课', PRIVATE: '私教' }[row.category] }}</template>
      </el-table-column>
      <el-table-column prop="timesCost" label="单次扣次" width="100" />
      <el-table-column prop="description" label="说明" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="500">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="团课" value="GROUP" />
            <el-option label="特色课" value="SPECIAL" />
            <el-option label="私教" value="PRIVATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="单次扣次"><el-input-number v-model="form.timesCost" :min="1" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
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
const form = reactive<any>({ id: null, name: '', category: 'GROUP', timesCost: 1, description: '', status: 1 })

const load = async () => {
  const r: any = await courseApi.typeList()
  list.value = r.data
}
const onAdd = () => { Object.assign(form, { id: null, name: '', category: 'GROUP', timesCost: 1, description: '', status: 1 }); dialogVisible.value = true }
const onEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const onDelete = async (row: any) => { await courseApi.typeDelete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => {
  if (!form.name) { ElMessage.warning('请填写课程名'); return }
  await courseApi.typeUpsert(form)
  ElMessage.success('已保存')
  dialogVisible.value = false
  load()
}
onMounted(load)
</script>
