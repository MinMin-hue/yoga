<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🏠 教室管理</h2>
        <div class="page-subtitle">配置瑜伽教室信息</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增教室</el-button>
    </div>

    <div class="room-grid">
      <div v-for="r in list" :key="r.id" class="room-card" :class="{ disabled: r.status !== 1 }">
        <div class="room-icon">🏠</div>
        <div class="room-name">{{ r.name }}</div>
        <div class="room-meta">
          <span class="label-chip">容量 {{ r.capacity }} 人</span>
          <span :class="['badge', r.status === 1 ? 'success' : 'muted']">
            <span class="badge-dot"></span>
            {{ r.status === 1 ? '使用中' : '停用' }}
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

      <div v-if="!list.length" class="empty-state">
        <div class="empty-icon">🏠</div>
        暂无教室, 点击右上角新增
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑教室' : '新增教室'" width="400">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="例如: 阳光教室 1" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" /></el-form-item>
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
const form = reactive<any>({ id: null, name: '', capacity: 20, status: 1 })

const load = async () => { const r: any = await courseApi.roomList(); list.value = r.data }
const onAdd = () => { Object.assign(form, { id: null, name: '', capacity: 20, status: 1 }); dialogVisible.value = true }
const onEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const onDelete = async (row: any) => { await courseApi.roomDelete(row.id); ElMessage.success('已删除'); load() }
const onSubmit = async () => { await courseApi.roomUpsert(form); ElMessage.success('已保存'); dialogVisible.value = false; load() }
onMounted(load)
</script>

<style scoped>
.room-grid {
  display: grid; gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}
.room-card {
  background: #fff; border-radius: var(--y-radius);
  padding: 24px 20px; text-align: center;
  box-shadow: var(--y-shadow-sm);
  transition: all .25s ease;
  position: relative; overflow: hidden;
}
.room-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
  background: var(--y-gradient-cool);
}
.room-card.disabled::before { background: #cbd5e1; }
.room-card:hover { transform: translateY(-3px); box-shadow: var(--y-shadow); }
.room-card.disabled { opacity: .65; }
.room-icon { font-size: 36px; margin-bottom: 10px; }
.room-name { font-size: 16px; font-weight: 600; color: var(--y-text); margin-bottom: 10px; }
.room-meta { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 12px; }
.room-actions { display: flex; justify-content: center; gap: 4px; padding-top: 10px; border-top: 1px dashed var(--y-border); }
</style>
