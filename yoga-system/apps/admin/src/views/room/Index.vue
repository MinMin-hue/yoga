<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🏠 教室管理</h2>
        <div class="page-subtitle">配置瑜伽教室与容纳人数</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增教室</el-button>
    </div>

    <div class="room-grid">
      <div v-for="r in list" :key="r.id" class="room-card">
        <div class="room-card-bar"></div>
        <div class="room-icon">🏠</div>
        <div class="room-name">{{ r.name }}</div>
        <div class="room-cap">
          <span class="badge info"><span class="badge-dot"></span>容量 {{ r.capacity }} 人</span>
        </div>
        <div class="room-status">
          <span :class="['badge', r.status === 1 ? 'success' : 'muted']">
            <span class="badge-dot"></span>
            {{ r.status === 1 ? '启用' : '停用' }}
          </span>
        </div>
        <div class="room-actions">
          <el-button size="small" link type="primary" @click="onEdit(r)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="onDelete(r)">
            <template #reference>
              <el-button size="small" link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑教室' : '新增教室'" width="440">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="例如: 一号教室" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
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
import { courseApi } from '@/api'

const list = ref<any[]>([])
const dialogVisible = ref(false)
const form = reactive<any>({ id: null, name: '', capacity: 12, status: 1 })

const load = async () => { const r: any = await courseApi.roomList(); list.value = r.data }
const onAdd = () => { Object.assign(form, { id: null, name: '', capacity: 12, status: 1 }); dialogVisible.value = true }
const onEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const onDelete = async (row: any) => { await courseApi.roomDelete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => {
  if (form.id) await courseApi.roomUpdate(form)
  else await courseApi.roomCreate(form)
  ElMessage.success('已保存'); dialogVisible.value = false; load()
}
onMounted(load)
</script>

<style scoped>
.room-grid {
  display: grid; gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}
.room-card {
  position: relative; background: #fff; border-radius: var(--y-radius);
  padding: 28px 20px 16px; text-align: center;
  box-shadow: var(--y-shadow-sm);
  transition: all .25s ease;
  overflow: hidden;
}
.room-card:hover { transform: translateY(-3px); box-shadow: var(--y-shadow); }
.room-card-bar {
  position: absolute; top: 0; left: 0; right: 0; height: 6px;
  background: var(--y-gradient-primary);
}
.room-icon {
  font-size: 36px; margin-bottom: 8px;
}
.room-name { font-size: 16px; font-weight: 700; color: var(--y-text); }
.room-cap, .room-status { margin: 8px 0; }
.room-actions {
  display: flex; gap: 6px; justify-content: center;
  margin-top: 12px; padding-top: 12px;
  border-top: 1px dashed var(--y-border);
}
</style>
