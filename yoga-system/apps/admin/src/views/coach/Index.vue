<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🧘 教练管理</h2>
        <div class="page-subtitle">在职教练列表</div>
      </div>
    </div>

    <div class="coach-grid">
      <div v-for="c in list" :key="c.id" class="coach-card">
        <div class="coach-avatar-lg">{{ (c.realName || c.username || '?').charAt(0) }}</div>
        <div class="coach-name">{{ c.realName || c.username }}</div>
        <div class="coach-username">@{{ c.username }}</div>
        <div class="coach-phone">
          <el-icon><Phone /></el-icon> {{ c.phone || '未设置' }}
        </div>
        <div class="coach-status">
          <span class="badge success"><span class="badge-dot"></span>在岗</span>
        </div>
      </div>

      <div v-if="!list.length" class="empty-state">
        <div class="empty-icon">🧘</div>暂无教练
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Phone } from '@element-plus/icons-vue'
import { courseApi } from '@/api'

const list = ref<any[]>([])
onMounted(async () => { const r: any = await courseApi.coachList(); list.value = r.data })
</script>

<style scoped>
.coach-grid {
  display: grid; gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}
.coach-card {
  background: #fff; border-radius: var(--y-radius);
  padding: 24px 20px; text-align: center;
  box-shadow: var(--y-shadow-sm);
  transition: all .25s ease;
  position: relative; overflow: hidden;
}
.coach-card::before {
  content: ''; position: absolute; top: -40px; right: -40px;
  width: 120px; height: 120px; border-radius: 50%;
  background: var(--y-gradient-warm); opacity: .08;
}
.coach-card:hover { transform: translateY(-3px); box-shadow: var(--y-shadow); }
.coach-avatar-lg {
  width: 72px; height: 72px; border-radius: 50%;
  background: var(--y-gradient-warm);
  color: #fff; margin: 0 auto 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 600;
  box-shadow: 0 6px 16px rgba(245, 87, 108, 0.3);
}
.coach-name { font-size: 16px; font-weight: 700; color: var(--y-text); }
.coach-username { font-size: 12px; color: var(--y-text-muted); margin-top: 2px; }
.coach-phone {
  font-size: 12px; color: var(--y-text-secondary);
  margin: 10px 0;
  display: flex; align-items: center; justify-content: center; gap: 4px;
}
</style>
