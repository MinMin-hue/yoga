<template>
  <div class="page">
    <!-- 欢迎横幅 -->
    <div class="page-hero">
      <h2>👋 你好,{{ userName }}!</h2>
      <p>今天是 {{ today }}, 祝你工作愉快, 期待今天的精彩课程 ✨</p>
      <div class="hero-actions">
        <el-button round size="default" @click="$router.push('/schedule')">
          <el-icon><Calendar /></el-icon>&nbsp;查看排课
        </el-button>
        <el-button round size="default" type="warning" @click="$router.push('/order')">
          <el-icon><Money /></el-icon>&nbsp;处理订单
        </el-button>
      </div>
    </div>

    <!-- 统计卡 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card primary">
          <div class="stat-icon">💰</div>
          <div class="stat-num">¥ {{ formatNum(data.revenue?.total) }}</div>
          <div class="stat-label">本月营业额</div>
          <div class="stat-trend">订单数 {{ data.revenue?.orderCount || 0 }} 笔</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card warm">
          <div class="stat-icon">👥</div>
          <div class="stat-num">{{ data.member?.newCount || 0 }}</div>
          <div class="stat-label">本月新增会员</div>
          <div class="stat-trend">活跃 {{ data.member?.activeCount || 0 }} 人</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card cool">
          <div class="stat-icon">📅</div>
          <div class="stat-num">{{ data.course?.reduce((s: number, c: any) => s + c.scheduleCount, 0) || 0 }}</div>
          <div class="stat-label">本月排课数</div>
          <div class="stat-trend">已约 {{ data.course?.reduce((s: number, c: any) => s + c.bookedCount, 0) || 0 }} 人次</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card success">
          <div class="stat-icon">🏆</div>
          <div class="stat-num">{{ data.coach?.length || 0 }}</div>
          <div class="stat-label">在职教练</div>
          <div class="stat-trend">本月服务 {{ data.coach?.reduce((s: number, c: any) => s + c.studentCount, 0) || 0 }} 学员</div>
        </div>
      </el-col>
    </el-row>

    <!-- 详细数据 -->
    <el-row :gutter="16" class="row-gap">
      <el-col :span="12">
        <div class="y-card">
          <div class="y-card-header">
            <span class="y-card-title">🔥 课程上座率 TOP</span>
            <el-button text type="primary" @click="$router.push('/statistics')">查看更多</el-button>
          </div>
          <el-table :data="(data.course || []).slice(0, 5)" size="default">
            <el-table-column prop="courseTypeName" label="课程" />
            <el-table-column prop="scheduleCount" label="排课" width="80" align="center" />
            <el-table-column prop="bookedCount" label="已约" width="80" align="center" />
            <el-table-column label="上座率" width="180">
              <template #default="{ row }">
                <div style="display:flex;align-items:center;gap:8px">
                  <div class="capacity-bar" :class="rateClass(row.rate)">
                    <span :style="{ width: Math.min(row.rate, 100) + '%' }"></span>
                  </div>
                  <span class="capacity-text">{{ row.rate }}%</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!data.course?.length" class="empty-state">
            <div class="empty-icon">📊</div>暂无数据
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="y-card">
          <div class="y-card-header">
            <span class="y-card-title">⭐ 教练课时榜</span>
            <el-button text type="primary" @click="$router.push('/coach')">教练列表</el-button>
          </div>
          <el-table :data="(data.coach || []).slice(0, 5)" size="default">
            <el-table-column label="教练" min-width="120">
              <template #default="{ row }">
                <div style="display:flex;align-items:center;gap:8px">
                  <div class="y-coach-avatar">{{ row.coachName?.charAt(0) }}</div>
                  <span>{{ row.coachName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="scheduleCount" label="排课" width="80" align="center" />
            <el-table-column prop="studentCount" label="学员" width="80" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default><span class="badge success"><span class="badge-dot"></span>在岗</span></template>
            </el-table-column>
          </el-table>
          <div v-if="!data.coach?.length" class="empty-state">
            <div class="empty-icon">🧘</div>暂无教练
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { statisticsApi } from '@/api'

const user = useUserStore()
const data = ref<any>({})

const userName = computed(() => user.profile?.realName || user.profile?.username || '管理员')

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const formatNum = (n: number) => Number(n || 0).toLocaleString('zh-CN')

const rateClass = (rate: number) => {
  if (rate >= 90) return 'full'
  if (rate >= 70) return 'warn'
  return ''
}

const load = async () => {
  const [rev, mem, course, coach] = await Promise.all([
    statisticsApi.revenue('month'),
    statisticsApi.member('month'),
    statisticsApi.course('month'),
    statisticsApi.coach('month')
  ])
  data.value = { revenue: rev.data, member: mem.data, course: course.data, coach: coach.data }
}
onMounted(load)
</script>

<style scoped>
.row-gap { margin-top: 16px; }

.y-coach-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--y-gradient-primary);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600;
  box-shadow: 0 2px 6px rgba(102,126,234,.3);
}
</style>
