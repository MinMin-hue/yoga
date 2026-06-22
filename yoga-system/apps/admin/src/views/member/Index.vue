<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="手机号/昵称" style="width: 200px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="success" @click="onAdd">新增会员</el-button>
    </div>
    <el-table :data="list" border v-loading="loading">
      <el-table-column type="index" width="50" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">{{ ['', '', '女', '男'][row.gender || 0] }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该会员?" @confirm="onDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @current-change="(p: number) => load(p)" layout="total, prev, pager, next" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会员' : '新增会员'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日"><el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
