<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📚 课程类型</h2>
        <div class="page-subtitle">配置团课、私教、特色课等课程类型</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="onAdd">新增课程类型</el-button>
    </div>

    <!-- 课程类型卡片网格 -->
    <div class="course-grid">
      <div v-for="item in list" :key="item.id" class="course-card" :class="catClass(item.category)">
        <div class="course-card-bg"></div>
        <div class="course-card-icon">{{ catIcon(item.category) }}</div>
        <div class="course-card-body">
          <div class="course-card-title">{{ item.name }}</div>
          <div class="course-card-meta">
            <span class="label-chip">{{ { GROUP: '团课', SPECIAL: '特色课', PRIVATE: '私教' }[item.category] }}</span>
            <span class="text-muted" style="font-size: 12px;">单次扣 {{ item.timesCost }} 次</span>
          </div>
          <div class="course-card-desc">{{ item.description || '暂无说明' }}</div>
          <div class="course-card-foot">
            <span :class="['badge', item.status === 1 ? 'success' : 'muted']">
              <span class="badge-dot"></span>
              {{ item.status === 1 ? '启用' : '停用' }}
            </span>
            <div>
              <el-button size="small" link type="primary" @click="onEdit(item)">编辑</el-button>
              <el-popconfirm title="确认删除?" @confirm="onDelete(item)">
                <template #reference>
                  <el-button size="small" link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="500">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名"><el-input v-model="form.name" placeholder="例如: 哈他瑜伽入门" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="团课 (Group)" value="GROUP" />
            <el-option label="特色课 (Special)" value="SPECIAL" />
            <el-option label="私教 (Private)" value="PRIVATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="单次扣次">
          <el-input-number v-model="form.timesCost" :min="1" />
          <span class="text-muted" style="margin-left: 8px;">预约一次扣除的次数</span>
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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
const form = reactive<any>({ id: null, name: '', category: 'GROUP', timesCost: 1, description: '', status: 1 })

const catIcon = (c: string) => ({ GROUP: '🧘‍♀️', SPECIAL: '✨', PRIVATE: '👤' } as any)[c] || '🧘'
const catClass = (c: string) => ({ GROUP: 'group', SPECIAL: 'special', PRIVATE: 'private' } as any)[c] || 'group'

const load = async () => { const r: any = await courseApi.typeList(); list.value = r.data }
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

<style scoped>
.course-grid {
  display: grid; gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}
.course-card {
  position: relative; overflow: hidden;
  background: #fff; border-radius: var(--y-radius);
  padding: 0; box-shadow: var(--y-shadow-sm);
  transition: all .25s ease;
  cursor: default;
}
.course-card:hover { transform: translateY(-4px); box-shadow: var(--y-shadow-xl); }
.course-card-bg {
  position: absolute; top: 0; left: 0; right: 0;
  height: 80px; opacity: .9;
}
.course-card.group .course-card-bg { background: var(--y-gradient-primary); }
.course-card.special .course-card-bg { background: var(--y-gradient-warm); }
.course-card.private .course-card-bg { background: var(--y-gradient-cool); }

.course-card-icon {
  position: absolute; top: 16px; right: 16px;
  width: 48px; height: 48px; border-radius: 12px;
  background: rgba(255,255,255,.25);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; backdrop-filter: blur(8px);
}

.course-card-body { padding: 80px 18px 16px; position: relative; z-index: 1; }
.course-card-title { font-size: 17px; font-weight: 700; color: var(--y-text); margin-bottom: 8px; }
.course-card-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.course-card-desc {
  font-size: 12px; color: var(--y-text-muted);
  min-height: 36px; line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.course-card-foot {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 12px; border-top: 1px dashed var(--y-border);
}
</style>
