<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="onAdd">新增卡类型</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="卡名" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag>{{ { TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' }[row.cardKind] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="validDays" label="有效天数" width="100" />
      <el-table-column prop="totalTimes" label="总次数" width="100" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="onDelete(row)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑卡类型' : '新增卡类型'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="卡名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型" prop="cardKind">
          <el-radio-group v-model="form.cardKind">
            <el-radio value="TIME">时间卡</el-radio>
            <el-radio value="TIMES">次卡</el-radio>
            <el-radio value="MIXED">混合卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="有效天数" v-if="form.cardKind === 'TIME' || form.cardKind === 'MIXED'">
          <el-input-number v-model="form.validDays" :min="1" />
        </el-form-item>
        <el-form-item label="总次数" v-if="form.cardKind === 'TIMES' || form.cardKind === 'MIXED'">
          <el-input-number v-model="form.totalTimes" :min="1" />
        </el-form-item>
        <el-form-item label="可约课程">
          <el-select v-model="form.applicableTypes" multiple placeholder="不限" style="width: 100%">
            <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { cardApi, courseApi } from '@/api'

const list = ref<any[]>([])
const courseTypes = ref<any[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<any>({ id: null, name: '', cardKind: 'TIME', price: 0, validDays: 30, totalTimes: 10, description: '', status: 1, applicableTypes: [] as number[] })
const rules = { name: [{ required: true, message: '请输入', trigger: 'blur' }], cardKind: [{ required: true, message: '请选择', trigger: 'change' }] }

const load = async () => {
  const r: any = await cardApi.typePage(1, 100)
  list.value = r.data.list
}
const loadCourseType = async () => {
  const r: any = await courseApi.typeList(1)
  courseTypes.value = r.data
}
const onAdd = () => { Object.assign(form, { id: null, name: '', cardKind: 'TIME', price: 0, validDays: 30, totalTimes: 10, description: '', status: 1, applicableTypes: [] }); dialogVisible.value = true }
const onEdit = (row: any) => { Object.assign(form, row, { applicableTypes: row.applicableTypes ? row.applicableTypes.split(',').map(Number) : [] }); dialogVisible.value = true }
const onDelete = async (row: any) => { await cardApi.typeDelete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await cardApi.typeUpdate(form)
    else await cardApi.typeCreate(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally { submitting.value = false }
}
onMounted(() => { load(); loadCourseType() })
</script>
