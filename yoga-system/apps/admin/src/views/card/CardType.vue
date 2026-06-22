<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">💳 会员卡类型</h2>
        <div class="page-subtitle">配置时间卡、次卡、混合卡等多种卡类型</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增卡类型</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="卡名" min-width="180">
          <template #default="{ row }">
            <div class="card-name">
              <div class="card-icon" :class="kindClass(row.cardKind)">{{ kindIcon(row.cardKind) }}</div>
              <div>
                <div class="card-title">{{ row.name }}</div>
                <div class="card-desc">{{ row.description || '暂无说明' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <span class="label-chip">{{ { TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' }[row.cardKind] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120" align="right">
          <template #default="{ row }">
            <span class="price" style="font-size: 18px;">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validDays" label="有效天数" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.validDays">{{ row.validDays }} 天</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalTimes" label="总次数" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.totalTimes">{{ row.totalTimes }} 次</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['badge', row.status === 1 ? 'success' : 'muted']">
              <span class="badge-dot"></span>
              {{ row.status === 1 ? '在售' : '下架' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="onEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该卡类型?" @confirm="onDelete(row)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑卡类型' : '新增卡类型'" width="560">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="卡名" prop="name"><el-input v-model="form.name" placeholder="例如: 季卡 30 次" /></el-form-item>
        <el-form-item label="卡类型" prop="cardKind">
          <el-radio-group v-model="form.cardKind">
            <el-radio-button value="TIME">⏰ 时间卡</el-radio-button>
            <el-radio-button value="TIMES">🔢 次卡</el-radio-button>
            <el-radio-button value="MIXED">🎯 混合卡</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效天数" v-if="form.cardKind === 'TIME' || form.cardKind === 'MIXED'">
          <el-input-number v-model="form.validDays" :min="1" />
          <span class="text-muted" style="margin-left: 8px;">自激活起多少天有效</span>
        </el-form-item>
        <el-form-item label="总次数" v-if="form.cardKind === 'TIMES' || form.cardKind === 'MIXED'">
          <el-input-number v-model="form.totalTimes" :min="1" />
          <span class="text-muted" style="margin-left: 8px;">可预约课程的总次数</span>
        </el-form-item>
        <el-form-item label="可约课程">
          <el-select v-model="form.applicableTypes" multiple placeholder="不限制, 所有课程可用" style="width: 100%">
            <el-option v-for="t in courseTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="卡的详细说明, 可选" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="在售" inactive-text="下架" />
        </el-form-item>
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
import { cardApi, courseApi } from '@/api'

const list = ref<any[]>([])
const courseTypes = ref<any[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<any>({ id: null, name: '', cardKind: 'TIME', price: 0, validDays: 30, totalTimes: 10, description: '', status: 1, applicableTypes: [] as number[] })
const rules = { name: [{ required: true, message: '请输入', trigger: 'blur' }], cardKind: [{ required: true, message: '请选择', trigger: 'change' }] }

const kindIcon = (k: string) => ({ TIME: '⏰', TIMES: '🔢', MIXED: '🎯' } as any)[k] || '💳'
const kindClass = (k: string) => ({ TIME: 'warm', TIMES: 'cool', MIXED: 'primary' } as any)[k] || 'primary'

const load = async () => { const r: any = await cardApi.typePage(1, 100); list.value = r.data.list }
const loadCourseType = async () => { const r: any = await courseApi.typeList(1); courseTypes.value = r.data }
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

<style scoped>
.card-name { display: flex; align-items: center; gap: 12px; }
.card-icon {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; color: #fff; flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0,0,0,.08);
}
.card-icon.primary { background: var(--y-gradient-primary); }
.card-icon.warm { background: var(--y-gradient-warm); }
.card-icon.cool { background: var(--y-gradient-cool); }
.card-title { font-size: 14px; font-weight: 600; color: var(--y-text); }
.card-desc { font-size: 12px; color: var(--y-text-muted); margin-top: 2px; }
</style>
